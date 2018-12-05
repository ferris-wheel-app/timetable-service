package com.ferris.timetable.model

import java.time.{Duration, LocalDate, LocalTime}
import java.util.UUID

object Model {

  case class TaskTemplate (
    taskId: Option[UUID],
    `type`: TaskTypes.TaskType
  )

  case class TimeBlockTemplate (
    start: LocalTime,
    finish: LocalTime,
    task: TaskTemplate
  ) {
    def durationInMillis: Long = {
      Duration.between(start, finish).toMillis
    }
  }

  case class TimetableTemplate (
    blocks: Seq[TimeBlockTemplate]
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
    sunday: TimetableTemplate,
    isCurrent: Boolean
  )

  case class ScheduledTask (
    taskId: UUID,
    `type`: TaskTypes.TaskType,
    isDone: Boolean
  )

  sealed trait ScheduledTimeBlock {
    def start: LocalTime
    def finish: LocalTime
  }

  case class ConcreteBlock (
    start: LocalTime,
    finish: LocalTime,
    task: ScheduledTask
  ) extends ScheduledTimeBlock

  case class BufferBlock (
    start: LocalTime,
    finish: LocalTime,
    firstTask: ScheduledTask,
    secondTask: ScheduledTask
  ) extends ScheduledTimeBlock

  case class Timetable (
    date: LocalDate,
    blocks: Seq[ScheduledTimeBlock]
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

    case object OneOff extends TaskType {
      override val dbValue = "ONE_OFF"
    }

    case object ScheduledOneOff extends TaskType {
      override val dbValue = "SCHEDULED_ONE_OFF"
    }

    case object BonusTime extends TaskType {
      override val dbValue = "BONUS_TIME"
    }
  }
}
