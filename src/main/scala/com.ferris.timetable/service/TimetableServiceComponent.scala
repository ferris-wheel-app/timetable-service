package com.ferris.timetable.service

import java.util.UUID

import com.ferris.timetable.command.Commands._
import com.ferris.timetable.model.Model.{Message, Routine, Timetable, TimetableTemplate}
import com.ferris.timetable.repo.TimetableRepositoryComponent

import scala.concurrent.{ExecutionContext, Future}

trait TimetableServiceComponent {
  val timetableService: TimetableService

  trait TimetableService {
    def createMessage(creation: CreateMessage)(implicit ex: ExecutionContext): Future[Message]
    def createRoutine(routine: CreateRoutine)(implicit ex: ExecutionContext): Future[Routine]
    def createTemplate(template: CreateTimetableTemplate)(implicit ex: ExecutionContext): Future[TimetableTemplate]
    def generateTimetable(implicit ex: ExecutionContext): Future[Timetable]

    def updateMessage(uuid: UUID, update: UpdateMessage)(implicit ex: ExecutionContext): Future[Message]
    def updateRoutine(uuid: UUID, update: UpdateRoutine)(implicit ex: ExecutionContext): Future[Routine]
    def startRoutine(uuid: UUID): Future[Boolean]
    def updateTemplate(uuid: UUID, update: UpdateTimetableTemplate)(implicit ex: ExecutionContext): Future[TimetableTemplate]

    def getMessages(implicit ex: ExecutionContext): Future[Seq[Message]]
    def getRoutines(implicit ex: ExecutionContext): Future[Seq[Routine]]
    def getTemplates(routineId: UUID)(implicit ex: ExecutionContext): Future[Seq[TimetableTemplate]]

    def getMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[Message]]
    def getRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[Routine]]
    def getTemplate(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[TimetableTemplate]]
    def currentTimetable(implicit ex: ExecutionContext): Future[Option[Timetable]]

    def deleteMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean]
    def deleteRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean]
    def deleteTemplate(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean]
  }
}

trait DefaultTimetableServiceComponent extends TimetableServiceComponent {
  this: TimetableRepositoryComponent =>

  override val timetableService = new DefaultTimetableService

  class DefaultTimetableService extends TimetableService {

    override def createMessage(creation: CreateMessage)(implicit ex: ExecutionContext): Future[Message] = {
      repo.createMessage(creation)
    }

    override def createRoutine(routine: CreateRoutine)(implicit ex: ExecutionContext) = ???

    override def createTemplate(template: CreateTimetableTemplate)(implicit ex: ExecutionContext) = ???

    override def generateTimetable(implicit ex: ExecutionContext) = ???

    override def updateMessage(uuid: UUID, update: UpdateMessage)(implicit ex: ExecutionContext): Future[Message] = {
      repo.updateMessage(uuid, update)
    }

    override def updateRoutine(uuid: UUID, update: UpdateRoutine)(implicit ex: ExecutionContext) = ???

    override def updateTemplate(uuid: UUID, update: UpdateTimetableTemplate)(implicit ex: ExecutionContext) = ???

    override def getMessages(implicit ex: ExecutionContext): Future[Seq[Message]] = {
      repo.getMessages
    }

    override def getMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[Message]] = {
      repo.getMessage(uuid)
    }

    override def getRoutines(implicit ex: ExecutionContext) = ???

    override def getRoutine(uuid: UUID)(implicit ex: ExecutionContext) = ???

    override def getTemplates(routineId: UUID)(implicit ex: ExecutionContext) = ???

    override def getTemplate(uuid: UUID)(implicit ex: ExecutionContext) = ???

    override def currentTimetable(implicit ex: ExecutionContext) = ???

    override def deleteMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean] = {
      repo.deleteMessage(uuid)
    }

    override def deleteRoutine(uuid: UUID)(implicit ex: ExecutionContext) = ???

    override def deleteTemplate(uuid: UUID)(implicit ex: ExecutionContext) = ???
  }
}
