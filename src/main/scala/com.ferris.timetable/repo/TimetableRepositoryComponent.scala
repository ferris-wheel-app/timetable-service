package com.ferris.timetable.repo

import java.util.UUID

import com.ferris.timetable.command.Commands._
import com.ferris.timetable.db.TablesComponent
import com.ferris.timetable.db.conversions.TableConversions
import com.ferris.timetable.model.Model._
import com.ferris.timetable.service.exceptions.Exceptions.MessageNotFoundException

import scala.concurrent.{ExecutionContext, Future}

trait TimetableRepositoryComponent {

  val repo: TimetableRepository

  trait TimetableRepository {
    def createMessage(creation: CreateMessage): Future[Message]
    def createRoutine(routine: CreateRoutine): Future[Routine]
    def createTemplate(template: CreateTimetableTemplate): Future[TimetableTemplate]
    def generateTimetable: Future[Timetable]

    def updateMessage(uuid: UUID, update: UpdateMessage): Future[Message]
    def updateRoutine(uuid: UUID, update: UpdateRoutine): Future[Routine]
    def updateTemplate(uuid: UUID, update: UpdateTimetableTemplate): Future[TimetableTemplate]

    def getMessages: Future[Seq[Message]]
    def getRoutines: Future[Seq[Routine]]
    def getTemplates(routineId: UUID): Future[Seq[TimetableTemplate]]

    def getMessage(uuid: UUID): Future[Option[Message]]
    def getRoutine(uuid: UUID): Future[Option[Routine]]
    def getTemplate(uuid: UUID): Future[Option[TimetableTemplate]]
    def currentTimetable: Future[Option[Timetable]]

    def deleteMessage(uuid: UUID): Future[Boolean]
    def deleteRoutine(uuid: UUID): Future[Boolean]
    def deleteTemplate(uuid: UUID): Future[Boolean]
  }
}

trait SqlTimetableRepositoryComponent extends TimetableRepositoryComponent {
  this: TablesComponent =>

  lazy val tableConversions = new TableConversions(tables)
  import tableConversions.tables._
  import tableConversions.tables.profile.api._
  import tableConversions._

  implicit val repoEc: ExecutionContext
  override val repo = new SqlTimetableRepository
  val db: tables.profile.api.Database

  class SqlTimetableRepository extends TimetableRepository {

    // Create endpoints
    override def createMessage(creation: CreateMessage): Future[Message] = {
      val row = MessageRow(
        id = 0L,
        uuid = UUID.randomUUID,
        sender = creation.sender,
        content = creation.content
      )
      val action = (MessageTable returning MessageTable.map(_.id) into ((message, id) => message.copy(id = id))) += row
      db.run(action) map (row => row.asMessage)
    }

    override def createRoutine(routine: CreateRoutine) = ???

    override def createTemplate(template: CreateTimetableTemplate) = ???

    override def generateTimetable = ???

    // Update endpoints
    override def updateMessage(uuid: UUID, update: UpdateMessage): Future[Message] = {
      val query = messageByUuid(uuid).map(message => (message.sender, message.content))
      val action = getMessageAction(uuid).flatMap { maybeObj =>
        maybeObj map { old =>
          query.update(update.sender.getOrElse(old.sender), update.content.getOrElse(old.content))
            .andThen(getMessageAction(uuid).map(_.head))
        } getOrElse DBIO.failed(MessageNotFoundException())
      }.transactionally
      db.run(action).map(row => row.asMessage)
    }

    override def updateRoutine(uuid: UUID, update: UpdateRoutine) = ???

    override def updateTemplate(uuid: UUID, update: UpdateTimetableTemplate) = ???

    // Get endpoints
    override def getMessages: Future[Seq[Message]] = {
      db.run(MessageTable.result.map(_.map(_.asMessage)))
    }

    override def getMessage(uuid: UUID): Future[Option[Message]] = {
      db.run(getMessageAction(uuid).map(_.map(_.asMessage)))
    }

    override def getRoutines = ???

    override def getRoutine(uuid: UUID) = ???

    override def getTemplates(routineId: UUID) = ???

    override def getTemplate(uuid: UUID) = ???

    override def currentTimetable = ???

    // Delete endpoints
    override def deleteMessage(uuid: UUID): Future[Boolean] = {
      val action = messageByUuid(uuid).delete
      db.run(action).map(_ > 0)
    }

    override def deleteRoutine(uuid: UUID) = ???

    override def deleteTemplate(uuid: UUID) = ???

    private def getMessageAction(uuid: UUID) = {
      messageByUuid(uuid).result.headOption
    }

    // Queries
    private def messageByUuid(uuid: UUID) = {
      MessageTable.filter(_.uuid === uuid.toString)
    }
  }
}
