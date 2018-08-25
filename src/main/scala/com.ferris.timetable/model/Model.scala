package com.ferris.timetable.model

import java.time.{LocalDate, LocalTime}
import java.util.UUID

object Model {

  case class Message (
    uuid: UUID,
    sender: String,
    content: String
  )

  case class Task (
    uuid: Option[UUID],
    `type`: TaskTypes.TaskType
  )

  case class TimeBlockTemplate (
    start: LocalTime,
    finish: LocalTime,
    task: Task
  )

  case class TimetableTemplate (
    blocks: Seq[TimeBlockTemplate]
  )

  case class ScheduledTask (
    uuid: Option[UUID],
    `type`: TaskTypes.TaskType,
    temporalStatus: TemporalStatuses.TemporalStatus
  )

  sealed trait TimeBlock {
    def start: LocalTime
    def finish: LocalTime
  }

  case class ConcreteBlock (
    start: LocalTime,
    finish: LocalTime,
    task: ScheduledTask
  ) extends TimeBlock

  case class BufferBlock (
    start: LocalTime,
    finish: LocalTime,
    firstTask: Task,
    secondTask: ScheduledTask
  ) extends TimeBlock

  case class Routine (
    uuid: UUID,
    name: String,
    monday: TimetableTemplate,
    tuesday: TimetableTemplate,
    wednesday: TimetableTemplate,
    thursday: TimetableTemplate,
    friday: TimetableTemplate,
    saturday: TimetableTemplate,
    sunday: TimetableTemplate,
    isCurrent: Boolean
  )

  case class Timetable (
    date: LocalDate,
    blocks: Seq[TimeBlock]
  )

  trait TypeEnum {
    def dbValue: String
  }

  sealed trait DayOfTheWeek extends TypeEnum

  object DayOfTheWeek {

    def withName(name: String): DayOfTheWeek = name match {
      case Monday.dbValue => Monday
      case Tuesday.dbValue => Tuesday
      case Wednesday.dbValue => Wednesday
      case Thursday.dbValue => Thursday
      case Friday.dbValue => Friday
      case Saturday.dbValue => Saturday
      case Sunday.dbValue => Sunday
    }

    case object Monday extends DayOfTheWeek {
      override val dbValue = "MONDAY"
    }

    case object Tuesday extends DayOfTheWeek {
      override val dbValue = "TUESDAY"
    }

    case object Wednesday extends DayOfTheWeek {
      override val dbValue = "WEDNESDAY"
    }

    case object Thursday extends DayOfTheWeek {
      override val dbValue = "THURSDAY"
    }

    case object Friday extends DayOfTheWeek {
      override val dbValue = "FRIDAY"
    }

    case object Saturday extends DayOfTheWeek {
      override val dbValue = "SATURDAY"
    }

    case object Sunday extends DayOfTheWeek {
      override val dbValue = "SUNDAY"
    }
  }

  object TaskTypes {

    sealed trait TaskType extends TypeEnum

    def withName(name: String): TaskType = name match {
      case Thread.dbValue => Thread
      case Weave.dbValue => Weave
      case LaserDonut.dbValue => LaserDonut
      case Hobby.dbValue => Hobby
    }

    case object Thread extends TaskType {
      override val dbValue = "THREAD"
    }

    case object Weave extends TaskType {
      override val dbValue = "WEAVE"
    }

    case object LaserDonut extends TaskType {
      override val dbValue = "LASER_DONUT"
    }

    case object Hobby extends TaskType {
      override val dbValue = "HOBBY"
    }
  }

  object TemporalStatuses {

    sealed trait TemporalStatus

    case object Previously extends TemporalStatus

    case object RightNow extends TemporalStatus

    case object Upcoming extends TemporalStatus
  }
}
