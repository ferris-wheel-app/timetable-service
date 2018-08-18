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

  case class UpdateTimeBlock (
    start: Option[LocalTime],
    finish: Option[LocalTime],
    task: Option[Task]
  )

  case class CreateTimetableTemplate (
    blocks: Seq[CreateTimeBlock]
  )

  case class UpdateTimetableTemplate (
    blocks: Option[Seq[UpdateTimeBlock]]
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
    monday: Option[UpdateTimetableTemplate],
    tuesday: Option[UpdateTimetableTemplate],
    wednesday: Option[UpdateTimetableTemplate],
    thursday: Option[UpdateTimetableTemplate],
    friday: Option[UpdateTimetableTemplate],
    saturday: Option[UpdateTimetableTemplate],
    sunday: Option[UpdateTimetableTemplate]
  )
}
