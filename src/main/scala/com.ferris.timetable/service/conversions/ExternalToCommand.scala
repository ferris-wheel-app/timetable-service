package com.ferris.timetable.service.conversions

import com.ferris.timetable.command.Commands.{CreateMessage, UpdateMessage}
import com.ferris.timetable.contract.resource.Resources.In.{MessageCreation, MessageUpdate}

object ExternalToCommand {

  sealed trait CommandConversion[T] {
    def toCommand: T
  }

  implicit class MessageCreationConversion(message: MessageCreation) extends CommandConversion[CreateMessage] {
    override def toCommand = CreateMessage(
      sender = message.sender,
      content = message.content
    )
  }

  implicit class MessageUpdateConversion(message: MessageUpdate) extends CommandConversion[UpdateMessage] {
    override def toCommand = UpdateMessage(
      sender = message.sender,
      content = message.content
    )
  }
}
