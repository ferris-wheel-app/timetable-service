package com.ferris.timetable.service.conversions

import com.ferris.timetable.command.Commands._
import com.ferris.timetable.contract.resource.Resources.In._

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

  implicit class TimeBlockCreationConversion(timeBlock: TimeBlockCreation) extends CommandConversion[CreateTimeBlock] {
    override def toCommand = CreateTimeBlock(
      start = timeBlock.start,
      finish = timeBlock.finish,
      task = timeBlock.task
    )
  }

  implicit class TimeBlockUpdateConversion(timeBlock: TimeBlockUpdate) extends CommandConversion[UpdateTimeBlock] {
    override def toCommand = UpdateTimeBlock(
      start = timeBlock.start,
      finish = timeBlock.finish,
      task = timeBlock.task
    )
  }

  implicit class TimetableTemplateCreationConversion(template: TimetableTemplateCreation) extends CommandConversion[CreateTimetableTemplate] {
    override def toCommand = CreateTimetableTemplate(
      day = TypeResolvers.DayOfTheWeek.withName(template.day),
      blocks = template.blocks.map(_.toCommand)
    )
  }

  implicit class TimetableTemplateUpdateConversion(template: TimetableTemplateUpdate) extends CommandConversion[UpdateTimetableTemplate] {
    override def toCommand = UpdateTimetableTemplate(
      day = template.day.map(TypeResolvers.DayOfTheWeek.withName),
      blocks = template.blocks.map(_.map(_.toCommand))
    )
  }

  implicit class RoutineCreationConversion(routine: RoutineCreation) extends CommandConversion[CreateRoutine] {
    override def toCommand = CreateRoutine(
      name = routine.name,
      templates = routine.templates.map(_.toCommand)
    )
  }

  implicit class RoutineUpdateConversion(routine: RoutineUpdate) extends CommandConversion[UpdateRoutine] {
    override def toCommand = UpdateRoutine(
      name = routine.name,
      templates = routine.templates.map(_.map(_.toCommand))
    )
  }
}
