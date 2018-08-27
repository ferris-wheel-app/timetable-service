package com.ferris.timetable.repo

import java.time.{DayOfWeek, LocalTime}
import java.util.UUID

import cats.data._
import com.ferris.timetable.command.Commands._
import com.ferris.timetable.db.DatabaseComponent
import com.ferris.timetable.db.conversions.DomainConversions
import com.ferris.timetable.model.Model._
import com.ferris.timetable.service.exceptions.Exceptions.{MessageNotFoundException, RoutineNotFoundException}
import com.ferris.utils.FerrisImplicits._
import com.ferris.utils.TimerComponent
import com.rms.miu.slickcats.DBIOInstances._

import scala.concurrent.ExecutionContext

trait TimetableRepositoryComponent {

  import slick.dbio.DBIO

  val repo: TimetableRepository

  trait TimetableRepository {
    def createMessage(message: CreateMessage): DBIO[Message]
    def createRoutine(routine: CreateRoutine): DBIO[Routine]
    def createTimetable(timetable: CreateTimetable): DBIO[Timetable]

    def updateMessage(uuid: UUID, update: UpdateMessage): DBIO[Message]
    def updateRoutine(uuid: UUID, update: UpdateRoutine): DBIO[Boolean]
    def startRoutine(uuid: UUID): DBIO[Boolean]
    def updateTimetable(update: UpdateTimetable): DBIO[Boolean]

    def getMessages: DBIO[Seq[Message]]
    def getRoutines: DBIO[Seq[Routine]]

    def getMessage(uuid: UUID): DBIO[Option[Message]]
    def getRoutine(uuid: UUID): DBIO[Option[Routine]]
    def currentTemplate: DBIO[Option[TimetableTemplate]]
    def currentTimetable: DBIO[Option[Timetable]]

    def deleteMessage(uuid: UUID): DBIO[Boolean]
    def deleteRoutine(uuid: UUID): DBIO[Boolean]
  }
}

trait SqlTimetableRepositoryComponent extends TimetableRepositoryComponent {
  this: DatabaseComponent with TimerComponent =>

  lazy val tableConversions = new DomainConversions(tables)
  import tableConversions.tables._
  import tableConversions.tables.profile.api._
  import tableConversions._

  implicit val repoEc: ExecutionContext
  override val repo = new SqlTimetableRepository

  class SqlTimetableRepository extends TimetableRepository {

    // Create endpoints
    override def createMessage(message: CreateMessage): DBIO[Message] = {
      val row = MessageRow(
        id = 0L,
        uuid = UUID.randomUUID,
        sender = message.sender,
        content = message.content
      )
      val action = (MessageTable returning MessageTable.map(_.id) into ((message, id) => message.copy(id = id))) += row
      action.map(_.asMessage)
    }

    override def createRoutine(routine: CreateRoutine): DBIO[Routine] = {
      (for {
        routineRow <- insertRoutine(routine)
        monday <- createWeeklyTemplate(routineRow.id, routine.monday, DayOfTheWeek.Monday)
        tuesday <- createWeeklyTemplate(routineRow.id, routine.tuesday, DayOfTheWeek.Tuesday)
        wednesday <- createWeeklyTemplate(routineRow.id, routine.wednesday, DayOfTheWeek.Wednesday)
        thursday <- createWeeklyTemplate(routineRow.id, routine.thursday, DayOfTheWeek.Thursday)
        friday <- createWeeklyTemplate(routineRow.id, routine.friday, DayOfTheWeek.Friday)
        saturday <- createWeeklyTemplate(routineRow.id, routine.saturday, DayOfTheWeek.Saturday)
        sunday <- createWeeklyTemplate(routineRow.id, routine.sunday, DayOfTheWeek.Sunday)
      } yield {
        Routine(
          uuid = UUID.fromString(routineRow.uuid),
          name = routineRow.name,
          monday = monday.asTimetableTemplate,
          tuesday = tuesday.asTimetableTemplate,
          wednesday = wednesday.asTimetableTemplate,
          thursday = thursday.asTimetableTemplate,
          friday = friday.asTimetableTemplate,
          saturday = saturday.asTimetableTemplate,
          sunday = sunday.asTimetableTemplate,
          isCurrent = routineRow.isCurrent
        )
      }).transactionally
    }

    override def createTimetable(timetable: CreateTimetable): DBIO[Timetable] = {
      val timeBlockRows = timetable.blocks.map { block =>
        ScheduledTimeBlockRow(
          id = 0L,
          date = java.sql.Date.valueOf(timetable.date),
          startTime = java.sql.Time.valueOf(block.start),
          finishTime = java.sql.Time.valueOf(block.finish),
          taskType = block.task.`type`.dbValue,
          taskId = block.task.taskId,
          isDone = false
        )
      }
      val action = (ScheduledTimeBlockTable returning ScheduledTimeBlockTable.map(_.id)) into ((timeBlock, id) => timeBlock.copy(id = id)) ++= timeBlockRows
      action.map(_.asTimetable(timetable.date))
    }

    // Update endpoints
    override def updateMessage(uuid: UUID, update: UpdateMessage): DBIO[Message] = {
      val query = messageByUuid(uuid).map(message => (message.sender, message.content))
      getMessage(uuid).flatMap { maybeObj =>
        maybeObj map { old =>
          query.update(update.sender.getOrElse(old.sender), update.content.getOrElse(old.content))
            .andThen(getMessage(uuid).map(_.head))
        } getOrElse DBIO.failed(MessageNotFoundException())
      }.transactionally
    }

    override def updateRoutine(uuid: UUID, update: UpdateRoutine): DBIO[Boolean] = {
      val query = routineByUuid(uuid).map(routine => routine.name)
      val existingRoutine = routineByUuid(uuid).result.headOption
      existingRoutine.flatMap { routineRow =>
        routineRow map { old =>
          (for {
            name <- query.update(update.name.getOrElse(old.name)).map(_ > 0)
            monday <- updateWeeklyTemplate(old.id, update.monday, DayOfTheWeek.Monday)
            tuesday <- updateWeeklyTemplate(old.id, update.tuesday, DayOfTheWeek.Tuesday)
            wednesday <- updateWeeklyTemplate(old.id, update.wednesday, DayOfTheWeek.Wednesday)
            thursday <- updateWeeklyTemplate(old.id, update.thursday, DayOfTheWeek.Thursday)
            friday <- updateWeeklyTemplate(old.id, update.friday, DayOfTheWeek.Friday)
            saturday <- updateWeeklyTemplate(old.id, update.saturday, DayOfTheWeek.Saturday)
            sunday <- updateWeeklyTemplate(old.id, update.sunday, DayOfTheWeek.Sunday)
          } yield name || monday || tuesday || wednesday || thursday || friday || saturday || sunday).transactionally
        } getOrElse DBIO.failed(RoutineNotFoundException())
      }.transactionally
    }

    override def startRoutine(uuid: UUID): DBIO[Boolean] = {
      for {
        otherRoutines <- RoutineTable.filterNot(_.uuid === uuid.toString).map(_.isCurrent).update(false)
        thisRoutine <- routineByUuid(uuid).map(_.isCurrent).update(true)
      } yield (otherRoutines :: thisRoutine :: Nil).forall(_ > 0)
    }

    override def updateTimetable(update: UpdateTimetable): DBIO[Boolean] = {
      def getSlot(start: LocalTime, finish: LocalTime) = {
        ScheduledTimeBlockTable.filter(row => row.startTime === start.toSqlTime && row.finishTime === finish.toSqlTime)
      }

      for {
        updates <- DBIO.sequence(update.blocks.map(block => getSlot(block.start, block.finish).map(_.isDone).update(block.done)))
      } yield updates.forall(_ > 0)
    }

    // Get endpoints
    override def getMessages: DBIO[Seq[Message]] = {
      MessageTable.result.map(_.map(_.asMessage))
    }

    override def getMessage(uuid: UUID): DBIO[Option[Message]] = {
      messageByUuid(uuid).result.headOption.map(_.map(_.asMessage))
    }

    override def getRoutines: DBIO[Seq[Routine]] = {
      (for {
        routineRows <- RoutineTable.result
        routines <- DBIO.sequence(routineRows.map(row => getRoutine(UUID.fromString(row.uuid))))
      } yield routines.flatten).transactionally
    }

    override def getRoutine(uuid: UUID): DBIO[Option[Routine]] = {
      (for {
        routine <- OptionT[DBIO, RoutineRow](routineByUuid(uuid).result.headOption)
        monday <- OptionT.liftF(getWeeklyTemplate(routine.id, DayOfTheWeek.Monday))
        tuesday <- OptionT.liftF(getWeeklyTemplate(routine.id, DayOfTheWeek.Tuesday))
        wednesday <- OptionT.liftF(getWeeklyTemplate(routine.id, DayOfTheWeek.Wednesday))
        thursday <- OptionT.liftF(getWeeklyTemplate(routine.id, DayOfTheWeek.Thursday))
        friday <- OptionT.liftF(getWeeklyTemplate(routine.id, DayOfTheWeek.Friday))
        saturday <- OptionT.liftF(getWeeklyTemplate(routine.id, DayOfTheWeek.Saturday))
        sunday <- OptionT.liftF(getWeeklyTemplate(routine.id, DayOfTheWeek.Sunday))
      } yield Routine(
        uuid = UUID.fromString(routine.uuid),
        name = routine.name,
        monday = monday.asTimetableTemplate,
        tuesday = tuesday.asTimetableTemplate,
        wednesday = wednesday.asTimetableTemplate,
        thursday = thursday.asTimetableTemplate,
        friday = friday.asTimetableTemplate,
        saturday = saturday.asTimetableTemplate,
        sunday = sunday.asTimetableTemplate,
        isCurrent = routine.isCurrent
      )).value.transactionally
    }

    override def currentTemplate: DBIO[Option[TimetableTemplate]] = {
      def today: DayOfTheWeek = timer.today.getDayOfWeek match {
        case DayOfWeek.MONDAY => DayOfTheWeek.Monday
        case DayOfWeek.TUESDAY => DayOfTheWeek.Tuesday
        case DayOfWeek.WEDNESDAY => DayOfTheWeek.Wednesday
        case DayOfWeek.THURSDAY => DayOfTheWeek.Thursday
        case DayOfWeek.FRIDAY => DayOfTheWeek.Friday
        case DayOfWeek.SATURDAY => DayOfTheWeek.Saturday
        case DayOfWeek.SUNDAY => DayOfTheWeek.Sunday
      }

      (for {
        currentRoutine <- OptionT[DBIO, RoutineRow](RoutineTable.filter(row => row.isCurrent === (1: Byte)).result.headOption)
        currentTemplate <- OptionT.liftF(getWeeklyTemplate(currentRoutine.id, today))
      } yield currentTemplate.asTimetableTemplate).value.transactionally
    }

    override def currentTimetable: DBIO[Option[Timetable]] = {
      for {
        currentSchedule <- ScheduledTimeBlockTable.filter(_.date === timer.today.toSqlDate).result
      } yield if (currentSchedule.nonEmpty) Some(currentSchedule.asTimetable(timer.today)) else None
    }

    // Delete endpoints
    override def deleteMessage(uuid: UUID): DBIO[Boolean] = {
      messageByUuid(uuid).delete.map(_ > 0)
    }

    override def deleteRoutine(uuid: UUID): DBIO[Boolean] = {
      def deleteActualRoutine(routineId: Long): DBIO[Boolean] = {
        RoutineTable.filter(_.id === routineId).delete.map(_ > 0)
      }
      (for {
        routine <- OptionT[DBIO, RoutineRow](routineByUuid(uuid).result.headOption)
        monday <- OptionT.liftF(deleteWeeklyTemplate(routine.id, DayOfTheWeek.Monday))
        tuesday <- OptionT.liftF(deleteWeeklyTemplate(routine.id, DayOfTheWeek.Tuesday))
        wednesday <- OptionT.liftF(deleteWeeklyTemplate(routine.id, DayOfTheWeek.Wednesday))
        thursday <- OptionT.liftF(deleteWeeklyTemplate(routine.id, DayOfTheWeek.Thursday))
        friday <- OptionT.liftF(deleteWeeklyTemplate(routine.id, DayOfTheWeek.Friday))
        saturday <- OptionT.liftF(deleteWeeklyTemplate(routine.id, DayOfTheWeek.Saturday))
        sunday <- OptionT.liftF(deleteWeeklyTemplate(routine.id, DayOfTheWeek.Sunday))
        routineDeleted <- OptionT.liftF(deleteActualRoutine(routine.id))
      } yield monday && tuesday && wednesday && thursday && friday && saturday && sunday && routineDeleted).value.map(_.getOrElse(true))
    }

    private def createWeeklyTemplate(routineId: Long, template: CreateTimetableTemplate, day: DayOfTheWeek): DBIO[Seq[TimeBlockRow]] = {
      for {
        timeBlocks <- insertTimeBlocks(template.blocks, day)
        _ <- linkRoutineToTemplate(routineId, timeBlocks, day)
      } yield timeBlocks
    }

    private def updateWeeklyTemplate(routineId: Long, template: Option[CreateTimetableTemplate], day: DayOfTheWeek): DBIO[Boolean] = {
      template match {
        case None => DBIO.successful(false)
        case Some(weeklyTemplate) => for {
          _ <- deleteWeeklyTemplate(routineId, day)
          newTimeBlocks <- createWeeklyTemplate(routineId, weeklyTemplate, day)
        } yield newTimeBlocks.nonEmpty
      }
    }

    private def getWeeklyTemplate(routineId: Long, day: DayOfTheWeek): DBIO[Seq[TimeBlockRow]] = {
      for {
        timeBlockIds <- routineTemplateLinks(routineId, day).map(_.timeBlockId).result
        timeBlocks <- timeBlocksById(timeBlockIds).result
      } yield timeBlocks
    }

    private def deleteWeeklyTemplate(routineId: Long, day: DayOfTheWeek): DBIO[Boolean] = {
      for {
        timeBlockIds <- routineTemplateLinks(routineId, day).map(_.timeBlockId).result
        linkDeleted <- routineTemplateLinks(routineId, day).delete
        routineDeleted <- timeBlocksById(timeBlockIds).delete
      } yield (linkDeleted :: routineDeleted :: Nil).forall(_ > 0)
    }

    private def insertRoutine(routine: CreateRoutine) = {
      (RoutineTable returning RoutineTable.map(_.id)) into ((routine, id) => routine.copy(id = id)) += RoutineRow(
        id = 0L,
        uuid = UUID.randomUUID,
        name = routine.name,
        isCurrent = false
      )
    }

    private def insertTimeBlocks(timeBlocks: Seq[CreateTimeBlockTemplate], day: DayOfTheWeek): DBIO[Seq[TimeBlockRow]] = {
      val timeBlockRows = timeBlocks.map { block =>
        TimeBlockRow(
          id = 0L,
          startTime = java.sql.Time.valueOf(block.start),
          finishTime = java.sql.Time.valueOf(block.finish),
          taskType = block.task.`type`.dbValue,
          taskId = block.task.taskId
        )
      }
      (TimeBlockTable returning TimeBlockTable.map(_.id)) into ((timeBlock, id) => timeBlock.copy(id = id)) ++= timeBlockRows
    }

    private def linkRoutineToTemplate(routineId: Long, timeBlocks: Seq[TimeBlockRow], day: DayOfTheWeek): DBIO[Seq[RoutineTimeBlockRow]] = {
      DBIO.sequence(timeBlocks.map { timeBlock =>
        (RoutineTimeBlockTable returning RoutineTimeBlockTable.map(_.id)) into ((link, id) => link.copy(id = id)) += RoutineTimeBlockRow(
          id = 0L,
          routineId = routineId,
          timeBlockId = timeBlock.id,
          dayOfWeek = day.dbValue
        )
      })
    }

    // Queries
    private def messageByUuid(uuid: UUID) = {
      MessageTable.filter(_.uuid === uuid.toString)
    }

    private def routineByUuid(uuid: UUID) = {
      RoutineTable.filter(_.uuid === uuid.toString)
    }

    private def routineTemplateLinks(routineId: Long, day: DayOfTheWeek) = {
      RoutineTimeBlockTable.filter(row => row.routineId === routineId && row.dayOfWeek === day.dbValue)
    }

    private def timeBlocksById(ids: Seq[Long]) = {
      TimeBlockTable.filter(_.id inSet ids)
    }
  }
}
