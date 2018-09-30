package com.ferris.timetable.service.conversions

import com.ferris.timetable.command.Commands._
import com.ferris.timetable.contract.resource.Resources.In._
import TypeResolvers._

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

  implicit class TaskRecordConversion(taskRecord: TaskTemplateCreation) extends CommandConversion[CreateTaskTemplate] {
    override def toCommand = CreateTaskTemplate(
      taskId = taskRecord.taskId,
      `type` = TaskType.withName(taskRecord.`type`)
    )
  }

  implicit class TimeBlockCreationConversion(timeBlock: TimeBlockTemplateCreation) extends CommandConversion[CreateTimeBlockTemplate] {
    override def toCommand = CreateTimeBlockTemplate(
      start = timeBlock.start,
      finish = timeBlock.finish,
      task = timeBlock.task.toCommand
    )
  }

  implicit class TimetableTemplateCreationConversion(template: TimetableTemplateCreation) extends CommandConversion[CreateTimetableTemplate] {
    override def toCommand = CreateTimetableTemplate(
      blocks = template.blocks.map(_.toCommand)
    )
  }

  implicit class RoutineCreationConversion(routine: RoutineCreation) extends CommandConversion[CreateRoutine] {
    override def toCommand = CreateRoutine(
      name = routine.name,
      monday = routine.monday.toCommand,
      tuesday = routine.tuesday.toCommand,
      wednesday = routine.wednesday.toCommand,
      thursday = routine.thursday.toCommand,
      friday = routine.friday.toCommand,
      saturday = routine.saturday.toCommand,
      sunday = routine.sunday.toCommand
    )
  }

  implicit class RoutineUpdateConversion(routine: RoutineUpdate) extends CommandConversion[UpdateRoutine] {
    override def toCommand = UpdateRoutine(
      name = routine.name,
      monday = routine.monday.map(_.toCommand),
      tuesday = routine.tuesday.map(_.toCommand),
      wednesday = routine.wednesday.map(_.toCommand),
      thursday = routine.thursday.map(_.toCommand),
      friday = routine.friday.map(_.toCommand),
      saturday = routine.saturday.map(_.toCommand),
      sunday = routine.sunday.map(_.toCommand)
    )
  }

  implicit class ScheduledTimeBlockUpdateConversion(timeBlock: ScheduledTimeBlockUpdate) extends CommandConversion[UpdateScheduledTimeBlock] {
    override def toCommand = UpdateScheduledTimeBlock(
      start = timeBlock.start,
      finish = timeBlock.finish,
      done = timeBlock.done
    )
  }

  implicit class TimetableUpdateConversion(template: TimetableUpdate) extends CommandConversion[UpdateTimetable] {
    override def toCommand = UpdateTimetable(
      blocks = template.blocks.map(_.toCommand)
    )
  }
}
