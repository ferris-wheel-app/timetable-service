package com.ferris.timetable.contract.resource

import java.time.{LocalDate, LocalTime}
import java.util.UUID
import com.ferris.timetable.contract.validation.InputValidators._

object Resources {

  object In {

    case class TaskTemplateCreation(
      taskId: Option[UUID],
      `type`: String
    ) {
      checkValidity(this)
    }

    case class TimeBlockTemplateCreation(
      start: LocalTime,
      finish: LocalTime,
      task: TaskTemplateCreation
    ) {
      checkValidity(this)
    }

    case class TimetableTemplateCreation (
      blocks: Seq[TimeBlockTemplateCreation]
    ) {
      checkValidity(this)
    }

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

    case class ScheduledTimeBlockUpdate(
      start: LocalTime,
      finish: LocalTime,
      done: Boolean
    )

    case class TimetableUpdate (
      blocks: Seq[ScheduledTimeBlockUpdate]
    ) {
      checkValidity(this)
    }
  }

  object Out {

    case class TaskTemplateView (
      taskId: Option[UUID],
      `type`: String,
      summary: Option[String]
    )

    case class TimeBlockTemplateView (
      start: LocalTime,
      finish: LocalTime,
      task: TaskTemplateView
    )

    case class TimetableTemplateView (
      blocks: Seq[TimeBlockTemplateView]
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

    case class ScheduledTaskView(
      taskId: UUID,
      `type`: String,
      summary: Option[String],
      isDone: Boolean
    )

    sealed trait ScheduledTimeBlockView {
      def start: LocalTime
      def finish: LocalTime
    }

    case class ConcreteBlockView (
      start: LocalTime,
      finish: LocalTime,
      task: ScheduledTaskView
    ) extends ScheduledTimeBlockView

    case class BufferBlockView (
      start: LocalTime,
      finish: LocalTime,
      firstTask: ScheduledTaskView,
      secondTask: ScheduledTaskView
    ) extends ScheduledTimeBlockView

    case class TimetableView (
      date: LocalDate,
      blocks: Seq[ScheduledTimeBlockView]
    )
  }
}
