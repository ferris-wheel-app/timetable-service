package com.ferris.timetable.service

import java.util.UUID

import com.ferris.timetable.command.Commands.{CreateMessage, UpdateMessage}
import com.ferris.timetable.model.Model.Message
import com.ferris.timetable.repo.TimetableRepositoryComponent

import scala.concurrent.{ExecutionContext, Future}

trait TimetableServiceComponent {
  val timetableService: TimetableService

  trait TimetableService {
    def createMessage(creation: CreateMessage)(implicit ex: ExecutionContext): Future[Message]

    def updateMessage(uuid: UUID, update: UpdateMessage)(implicit ex: ExecutionContext): Future[Message]

    def getMessages(implicit ex: ExecutionContext): Future[Seq[Message]]

    def getMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[Message]]

    def deleteMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean]
  }
}

trait DefaultTimetableServiceComponent extends TimetableServiceComponent {
  this: TimetableRepositoryComponent =>

  override val timetableService = new DefaultTimetableService

  class DefaultTimetableService extends TimetableService {

    override def createMessage(creation: CreateMessage)(implicit ex: ExecutionContext): Future[Message] = {
      repo.createMessage(creation)
    }

    override def updateMessage(uuid: UUID, update: UpdateMessage)(implicit ex: ExecutionContext): Future[Message] = {
      repo.updateMessage(uuid, update)
    }

    override def getMessages(implicit ex: ExecutionContext): Future[Seq[Message]] = {
      repo.getMessages
    }

    override def getMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[Message]] = {
      repo.getMessage(uuid)
    }

    override def deleteMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean] = {
      repo.deleteMessage(uuid)
    }
  }
}
