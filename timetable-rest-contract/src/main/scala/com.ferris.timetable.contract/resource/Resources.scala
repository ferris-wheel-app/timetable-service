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
      monday: TimetableTemplateCreation,
      tuesday: TimetableTemplateCreation,
      wednesday: TimetableTemplateCreation,
      thursday: TimetableTemplateCreation,
      friday: TimetableTemplateCreation,
      saturday: TimetableTemplateCreation,
      sunday: TimetableTemplateCreation
    )

    case class RoutineUpdate (
      name: Option[String],
      monday: Option[TimetableTemplateUpdate],
      tuesday: Option[TimetableTemplateUpdate],
      wednesday: Option[TimetableTemplateUpdate],
      thursday: Option[TimetableTemplateUpdate],
      friday: Option[TimetableTemplateUpdate],
      saturday: Option[TimetableTemplateUpdate],
      sunday: Option[TimetableTemplateUpdate]
    )

//    case class TimetableUpdate (
//      blocks: Option[Seq[TimeBlockUpdate]]
//    )
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
      start: LocalDateTime,
      finish: LocalDateTime,
      task: Option[UUID]
    ) extends TimeBlockView

    case class BufferBlockView (
      start: LocalDateTime,
      finish: LocalDateTime,
      firstTask: Option[UUID],
      secondTask: Option[UUID]
    ) extends TimeBlockView

    case class TimetableTemplateView (
      blocks: Seq[TimeBlockView]
    )

    case class RoutineView (
      uuid: UUID,
      name: String,
      monday: TimetableTemplateView,
      tuesday: TimetableTemplateView,
      wednesday: TimetableTemplateView,
      thursday: TimetableTemplateView,
      friday: TimetableTemplateView,
      saturday: TimetableTemplateView,
      sunday: TimetableTemplateView
    )

    case class TimetableView (
      date: LocalDate,
      blocks: Seq[TimeBlockView]
    )
  }
}
