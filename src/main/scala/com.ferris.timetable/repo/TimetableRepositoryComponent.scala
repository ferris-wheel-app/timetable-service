package com.ferris.timetable.repo

import java.util.UUID

import com.ferris.timetable.command.Commands._
import com.ferris.timetable.db.DatabaseComponent
import com.ferris.timetable.db.conversions.TableConversions
import com.ferris.timetable.model.Model._
import com.ferris.timetable.service.exceptions.Exceptions.MessageNotFoundException

import scala.concurrent.{ExecutionContext, Future}

trait TimetableRepositoryComponent {

  import slick.dbio.DBIO

  val repo: TimetableRepository

  trait TimetableRepository {
    def createMessage(creation: CreateMessage): DBIO[Message]
    def createRoutine(routine: CreateRoutine): DBIO[Routine]
    def createTemplate(template: CreateTimetableTemplate): DBIO[TimetableTemplate]

    def updateMessage(uuid: UUID, update: UpdateMessage): DBIO[Message]
    def updateRoutine(uuid: UUID, update: UpdateRoutine): DBIO[Routine]
    def updateTemplate(uuid: UUID, update: UpdateTimetableTemplate): DBIO[TimetableTemplate]

    def getMessages: DBIO[Seq[Message]]
    def getRoutines: DBIO[Seq[Routine]]
    def getTemplates(routineId: UUID): DBIO[Seq[TimetableTemplate]]

    def getMessage(uuid: UUID): DBIO[Option[Message]]
    def getRoutine(uuid: UUID): DBIO[Option[Routine]]
    def getTemplate(uuid: UUID): DBIO[Option[TimetableTemplate]]
    def currentTimetable: DBIO[Option[Timetable]]

    def deleteMessage(uuid: UUID): DBIO[Boolean]
    def deleteRoutine(uuid: UUID): DBIO[Boolean]
    def deleteTemplate(uuid: UUID): DBIO[Boolean]
  }
}

trait SqlTimetableRepositoryComponent extends TimetableRepositoryComponent {
  this: DatabaseComponent =>

  lazy val tableConversions = new TableConversions(tables)
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

    override def createRoutine(routine: CreateRoutine) = {
//      val row = RoutineRow(
//        id = 0L,
//        uuid = UUID.randomUUID,
//        name = routine.name,
//        isCurrent = false
//      )
//      val action = (RoutineTable returning RoutineTable.map(_.id)) into ((routine, id) => routine.copy(id = id)) += row
//      db.run(action) map (row => row)
      ???
    }

    override def createTemplate(template: CreateTimetableTemplate) = ???

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

    override def updateTemplate(uuid: UUID, update: UpdateTimetableTemplate) = ???

    // Get endpoints
    override def getMessages: DBIO[Seq[Message]] = {
      MessageTable.result.map(_.map(_.asMessage))
    }

    override def getMessage(uuid: UUID): DBIO[Option[Message]] = {
      messageByUuid(uuid).result.headOption.map(_.map(_.asMessage))
    }

    override def getRoutines = ???

    override def getRoutine(uuid: UUID) = ???

    override def getTemplates(routineId: UUID) = ???

    override def getTemplate(uuid: UUID) = ???

    override def currentTimetable = ???

    // Delete endpoints
    override def deleteMessage(uuid: UUID): DBIO[Boolean] = {
      messageByUuid(uuid).delete.map(_ > 0)
    }

    override def deleteRoutine(uuid: UUID) = ???

    override def deleteTemplate(uuid: UUID) = ???

    // Queries
    private def messageByUuid(uuid: UUID) = {
      MessageTable.filter(_.uuid === uuid.toString)
    }
  }
}
