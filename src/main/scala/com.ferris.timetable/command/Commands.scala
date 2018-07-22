package com.ferris.timetable.command

import java.time.LocalDateTime
import java.util.UUID

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

  case class CreateTimetable (
    date: LocalDateTime,
    blocks: Seq[CreateTimeBlock]
  )

  case class UpdateTimetable (
    date: LocalDateTime,
    blocks: Seq[UpdateTimeBlock]
  )
}
