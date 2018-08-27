package com.ferris.timetable.command

import java.time.{LocalDate, LocalTime}
import java.util.UUID

import com.ferris.timetable.model.Model.TaskTypes

object Commands {

  case class CreateMessage(sender: String, content: String)

  case class UpdateMessage(sender: Option[String], content: Option[String])

  case class CreateTaskTemplate (
    taskId: Option[UUID],
    `type`: TaskTypes.TaskType
  )

  case class CreateTimeBlockTemplate(
    start: LocalTime,
    finish: LocalTime,
    task: CreateTaskTemplate
  )

  case class CreateTimetableTemplate (
    blocks: Seq[CreateTimeBlockTemplate]
  )

  case class CreateRoutine (
    name: String,
    monday: CreateTimetableTemplate,
    tuesday: CreateTimetableTemplate,
    wednesday: CreateTimetableTemplate,
    thursday: CreateTimetableTemplate,
    friday: CreateTimetableTemplate,
    saturday: CreateTimetableTemplate,
    sunday: CreateTimetableTemplate
  )

  case class UpdateRoutine (
    name: Option[String],
    monday: Option[CreateTimetableTemplate],
    tuesday: Option[CreateTimetableTemplate],
    wednesday: Option[CreateTimetableTemplate],
    thursday: Option[CreateTimetableTemplate],
    friday: Option[CreateTimetableTemplate],
    saturday: Option[CreateTimetableTemplate],
    sunday: Option[CreateTimetableTemplate]
  )

  case class CreateScheduledTask (
    taskId: UUID,
    `type`: TaskTypes.TaskType
  )

  case class CreateScheduledTimeBlock (
    start: LocalTime,
    finish: LocalTime,
    task: CreateScheduledTask
  )

  case class UpdateScheduledTimeBlock (
    start: LocalTime,
    finish: LocalTime,
    done: Boolean
  )

  case class CreateTimetable (
    date: LocalDate,
    blocks: Seq[CreateScheduledTimeBlock]
  )

  case class UpdateTimetable (
    blocks: Seq[UpdateScheduledTimeBlock]
  )
}
