package com.ferris.timetable.service

import java.util.UUID

import com.ferris.timetable.command.Commands._
import com.ferris.timetable.db.DatabaseComponent
import com.ferris.timetable.model.Model.{Message, Routine, Timetable, TimetableTemplate}
import com.ferris.timetable.repo.TimetableRepositoryComponent

import scala.concurrent.{ExecutionContext, Future}

trait TimetableServiceComponent {
  val timetableService: TimetableService

  trait TimetableService {
    def createMessage(creation: CreateMessage)(implicit ex: ExecutionContext): Future[Message]
    def createRoutine(routine: CreateRoutine)(implicit ex: ExecutionContext): Future[Routine]
    def generateTimetable(implicit ex: ExecutionContext): Future[Timetable]

    def updateMessage(uuid: UUID, update: UpdateMessage)(implicit ex: ExecutionContext): Future[Message]
    def updateRoutine(uuid: UUID, update: UpdateRoutine)(implicit ex: ExecutionContext): Future[Routine]
    def startRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean]

    def getMessages(implicit ex: ExecutionContext): Future[Seq[Message]]
    def getRoutines(implicit ex: ExecutionContext): Future[Seq[Routine]]

    def getMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[Message]]
    def getRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[Routine]]
    def currentTimetable(implicit ex: ExecutionContext): Future[Option[Timetable]]

    def deleteMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean]
    def deleteRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean]
  }
}

trait DefaultTimetableServiceComponent extends TimetableServiceComponent {
  this: TimetableRepositoryComponent with DatabaseComponent =>

  override val timetableService = new DefaultTimetableService

  class DefaultTimetableService extends TimetableService {

    override def createMessage(creation: CreateMessage)(implicit ex: ExecutionContext): Future[Message] = {
      db.run(repo.createMessage(creation))
    }

    override def createRoutine(routine: CreateRoutine)(implicit ex: ExecutionContext) = ???

    override def generateTimetable(implicit ex: ExecutionContext) = ???

    override def updateMessage(uuid: UUID, update: UpdateMessage)(implicit ex: ExecutionContext): Future[Message] = {
      db.run(repo.updateMessage(uuid, update))
    }

    override def updateRoutine(uuid: UUID, update: UpdateRoutine)(implicit ex: ExecutionContext) = ???

    override def startRoutine(uuid: UUID)(implicit ex: ExecutionContext) = ???

    override def getMessages(implicit ex: ExecutionContext): Future[Seq[Message]] = {
      db.run(repo.getMessages)
    }

    override def getMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[Message]] = {
      db.run(repo.getMessage(uuid))
    }

    override def getRoutines(implicit ex: ExecutionContext) = ???

    override def getRoutine(uuid: UUID)(implicit ex: ExecutionContext) = ???

    override def currentTimetable(implicit ex: ExecutionContext) = ???

    override def deleteMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean] = {
      db.run(repo.deleteMessage(uuid))
    }

    override def deleteRoutine(uuid: UUID)(implicit ex: ExecutionContext) = ???
  }
}
