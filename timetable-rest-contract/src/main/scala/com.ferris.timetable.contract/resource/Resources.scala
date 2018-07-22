package com.ferris.timetable.contract.resource

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

object Resources {

  object In {

    case class MessageCreation (
      sender: String,
      content: String
    )

    case class MessageUpdate (
      sender: Option[String],
      content: Option[String]
    )

    case class TimeBlockCreation (
      start: LocalDateTime,
      finish: LocalDateTime,
      task: Option[UUID]
    )

    case class TimeBlockUpdate (
      start: Option[LocalDateTime],
      finish: Option[LocalDateTime],
      task: Option[UUID]
    )
  }

  object Out {

    case class MessageView (
      uuid: UUID,
      sender: String,
      content: String
    )

    sealed trait TimeBlockView {
      def start: LocalDateTime
      def finish: LocalDateTime
    }

    case class ConcreteBlockView (
      uuid: UUID,
      start: LocalDateTime,
      finish: LocalDateTime,
      task: Option[UUID]
    )

    case class BufferBlockView (
      uuid: UUID,
      start: LocalDateTime,
      finish: LocalDateTime,
      firstTask: Option[UUID],
      secondTask: Option[UUID]
    )

    case class TimetableView (
      date: LocalDate,
      blocks: Seq[TimeBlockView]
    )
  }
}
