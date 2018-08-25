package com.ferris.timetable.command

import java.time.LocalTime
import java.util.UUID

import com.ferris.timetable.model.Model.Task

object Commands {

  case class CreateMessage(sender: String, content: String)

  case class UpdateMessage(sender: Option[String], content: Option[String])

  case class CreateTimeBlock (
    start: LocalTime,
    finish: LocalTime,
    task: Task
  )

  case class CreateScheduledTimeBlock (
    start: LocalTime,
    finish: LocalTime,
    task: Task
  )

  case class CreateTimetableTemplate (
    blocks: Seq[CreateTimeBlock]
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
}
