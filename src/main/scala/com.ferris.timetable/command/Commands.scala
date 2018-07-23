package com.ferris.timetable.command

import java.time.LocalDateTime
import java.util.UUID

import com.ferris.timetable.model.Model.DaysOfTheWeek.DayOfTheWeek

object Commands {

  case class CreateMessage(sender: String, content: String)

  case class UpdateMessage(sender: Option[String], content: Option[String])

  case class CreateTimeBlock (
    start: LocalDateTime,
    finish: LocalDateTime,
    task: Option[UUID]
  )

  case class UpdateTimeBlock (
    start: Option[LocalDateTime],
    finish: Option[LocalDateTime],
    task: Option[UUID]
  )

  case class CreateTimetableTemplate (
    day: DayOfTheWeek,
    blocks: Seq[CreateTimeBlock]
  )

  case class UpdateTimetableTemplate (
    day: DayOfTheWeek,
    blocks: Seq[UpdateTimeBlock]
  )

  case class CreateRoutine (
    name: String,
    templates: Seq[CreateTimetableTemplate]
  )

  case class UpdateRoutine (
    name: Option[String],
    templates: Option[Seq[CreateTimetableTemplate]]
  )
}
