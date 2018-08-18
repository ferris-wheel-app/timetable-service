package com.ferris.timetable.repo

import java.util.UUID

import com.ferris.timetable.command.Commands._
import com.ferris.timetable.db.DatabaseComponent
import com.ferris.timetable.db.conversions.DomainConversions
import com.ferris.timetable.model.Model._
import com.ferris.timetable.service.exceptions.Exceptions.MessageNotFoundException

import scala.concurrent.ExecutionContext

trait TimetableRepositoryComponent {

  import slick.dbio.DBIO

  val repo: TimetableRepository

  trait TimetableRepository {
    def createMessage(creation: CreateMessage): DBIO[Message]
    def createRoutine(routine: CreateRoutine): DBIO[Routine]

    def updateMessage(uuid: UUID, update: UpdateMessage): DBIO[Message]
    def updateRoutine(uuid: UUID, update: UpdateRoutine): DBIO[Routine]

    def getMessages: DBIO[Seq[Message]]
    def getRoutines: DBIO[Seq[Routine]]

    def getMessage(uuid: UUID): DBIO[Option[Message]]
    def getRoutine(uuid: UUID): DBIO[Option[Routine]]
    def currentTimetable: DBIO[Option[Timetable]]

    def deleteMessage(uuid: UUID): DBIO[Boolean]
    def deleteRoutine(uuid: UUID): DBIO[Boolean]
  }
}

trait SqlTimetableRepositoryComponent extends TimetableRepositoryComponent {
  this: DatabaseComponent =>

  lazy val tableConversions = new DomainConversions(tables)
  import tableConversions.tables._
  import tableConversions.tables.profile.api._
  import tableConversions._

  implicit val repoEc: ExecutionContext
  override val repo = new SqlTimetableRepository

  class SqlTimetableRepository extends TimetableRepository {

    // Create endpoints
    override def createMessage(creation: CreateMessage): DBIO[Message] = {
      val row = MessageRow(
        id = 0L,
        uuid = UUID.randomUUID,
        sender = creation.sender,
        content = creation.content
      )
      val action = (MessageTable returning MessageTable.map(_.id) into ((message, id) => message.copy(id = id))) += row
      action.map(_.asMessage)
    }

    override def createRoutine(routine: CreateRoutine): DBIO[Routine] = {
      def insertRoutine() = {
        (RoutineTable returning RoutineTable.map(_.id)) into ((routine, id) => routine.copy(id = id)) += RoutineRow(
          id = 0L,
          uuid = UUID.randomUUID,
          name = routine.name,
          isCurrent = false
        )
      }

      def insertTemplate(template: CreateTimetableTemplate, day: DayOfTheWeek): DBIO[Seq[TimeBlockRow]] = {
        val timeBlockRows = template.blocks.map { block =>
          TimeBlockRow(
            id = 0L,
            startTime = java.sql.Time.valueOf(block.start),
            finishTime = java.sql.Time.valueOf(block.finish),
            taskType = block.task.`type`.dbValue,
            taskId = block.task.uuid
          )
        }
        (TimeBlockTable returning TimeBlockTable.map(_.id)) into ((timeBlock, id) => timeBlock.copy(id = id)) ++= timeBlockRows
      }

      def linkRoutineToTemplate(routineId: Long, timeBlocks: Seq[TimeBlockRow], day: DayOfTheWeek): DBIO[Seq[RoutineTimeBlockRow]] = {
        DBIO.sequence(timeBlocks.map { timeBlock =>
          (RoutineTimeBlockTable returning RoutineTimeBlockTable.map(_.id)) into ((link, id) => link.copy(id = id)) += RoutineTimeBlockRow(
            id = 0L,
            routineId = routineId,
            timeBlockId = timeBlock.id,
            dayOfWeek = day.dbValue
          )
        })
      }

      def createWeeklyRoutine(routineId: Long, template: CreateTimetableTemplate, day: DayOfTheWeek): DBIO[Seq[TimeBlockRow]] = {
        for {
          timeBlocks <- insertTemplate(template, day)
          _ <- linkRoutineToTemplate(routineId, timeBlocks, day)
        } yield timeBlocks
      }

      for {
        routineRow <- insertRoutine()
        monday <- createWeeklyRoutine(routineRow.id, routine.monday, DayOfTheWeek.Monday)
        tuesday <- createWeeklyRoutine(routineRow.id, routine.tuesday, DayOfTheWeek.Tuesday)
        wednesday <- createWeeklyRoutine(routineRow.id, routine.wednesday, DayOfTheWeek.Wednesday)
        thursday <- createWeeklyRoutine(routineRow.id, routine.thursday, DayOfTheWeek.Thursday)
        friday <- createWeeklyRoutine(routineRow.id, routine.friday, DayOfTheWeek.Friday)
        saturday <- createWeeklyRoutine(routineRow.id, routine.saturday, DayOfTheWeek.Saturday)
        sunday <- createWeeklyRoutine(routineRow.id, routine.sunday, DayOfTheWeek.Sunday)
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
      }
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

    override def updateRoutine(uuid: UUID, update: UpdateRoutine) = ???

    // Get endpoints
    override def getMessages: DBIO[Seq[Message]] = {
      MessageTable.result.map(_.map(_.asMessage))
    }

    override def getMessage(uuid: UUID): DBIO[Option[Message]] = {
      messageByUuid(uuid).result.headOption.map(_.map(_.asMessage))
    }

    override def getRoutines = ???

    override def getRoutine(uuid: UUID) = ???

    override def currentTimetable = ???

    // Delete endpoints
    override def deleteMessage(uuid: UUID): DBIO[Boolean] = {
      messageByUuid(uuid).delete.map(_ > 0)
    }

    override def deleteRoutine(uuid: UUID) = ???

    // Queries
    private def messageByUuid(uuid: UUID) = {
      MessageTable.filter(_.uuid === uuid.toString)
    }
  }
}
