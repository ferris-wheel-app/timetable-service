package com.ferris.timetable.contract.resource

import java.time.{LocalDate, LocalTime}
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

    case class TaskRecord (
      uuid: Option[UUID],
      `type`: String
    )

    case class TimeBlockCreation (
      start: LocalTime,
      finish: LocalTime,
      task: TaskRecord
    )

    case class TimeBlockUpdate (
      start: Option[LocalTime],
      finish: Option[LocalTime],
      task: Option[TaskRecord]
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
      monday: Option[TimetableTemplateCreation],
      tuesday: Option[TimetableTemplateCreation],
      wednesday: Option[TimetableTemplateCreation],
      thursday: Option[TimetableTemplateCreation],
      friday: Option[TimetableTemplateCreation],
      saturday: Option[TimetableTemplateCreation],
      sunday: Option[TimetableTemplateCreation]
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

    case class TaskView (
      `type`: String,
      summary: Option[String]
    )

    sealed trait TimeBlockView {
      def start: LocalTime
      def finish: LocalTime
    }

    case class ConcreteBlockView (
      start: LocalTime,
      finish: LocalTime,
      task: TaskView
    ) extends TimeBlockView

    case class BufferBlockView (
      start: LocalTime,
      finish: LocalTime,
      firstTask: TaskView,
      secondTask: TaskView
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
