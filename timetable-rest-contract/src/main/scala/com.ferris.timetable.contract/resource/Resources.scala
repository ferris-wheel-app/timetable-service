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

    case class TimetableTemplateCreation (
      day: String,
      blocks: Seq[TimeBlockCreation]
    )

    case class TimetableTemplateUpdate (
      day: Option[String],
      blocks: Option[Seq[TimeBlockUpdate]]
    )

    case class RoutineCreation (
      name: String,
      templates: Seq[TimetableTemplateCreation]
    )

    case class RoutineUpdate (
      name: Option[String],
      templates: Option[Seq[TimetableTemplateUpdate]]
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
    ) extends TimeBlockView

    case class BufferBlockView (
      uuid: UUID,
      start: LocalDateTime,
      finish: LocalDateTime,
      firstTask: Option[UUID],
      secondTask: Option[UUID]
    ) extends TimeBlockView

    case class TimetableTemplateView (
      uuid: UUID,
      day: String,
      blocks: Seq[TimeBlockView]
    )

    case class RoutineView (
      uuid: UUID,
      name: String,
      templates: Seq[TimetableTemplateView]
    )

    case class TimetableView (
      date: LocalDate,
      blocks: Seq[TimeBlockView]
    )
  }
}
