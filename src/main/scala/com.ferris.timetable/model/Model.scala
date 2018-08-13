package com.ferris.timetable.model

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

object Model {

  case class Message (
    uuid: UUID,
    sender: String,
    content: String
  )

  sealed trait TimeBlock {
    def start: LocalDateTime
    def finish: LocalDateTime
  }

  case class ConcreteBlock (
    start: LocalDateTime,
    finish: LocalDateTime,
    task: Option[UUID]
  ) extends TimeBlock

  case class BufferBlock (
    start: LocalDateTime,
    finish: LocalDateTime,
    firstTask: Option[UUID],
    secondTask: Option[UUID]
  ) extends TimeBlock

  case class TimetableTemplate (
    blocks: Seq[TimeBlock]
  )

  case class Routine (
    uuid: UUID,
    name: String,
    monday: TimetableTemplate,
    tuesday: TimetableTemplate,
    wednesday: TimetableTemplate,
    thursday: TimetableTemplate,
    friday: TimetableTemplate,
    saturday: TimetableTemplate,
    sunday: TimetableTemplate
  )

  case class Timetable (
    date: LocalDate,
    blocks: Seq[TimeBlock]
  )
}
