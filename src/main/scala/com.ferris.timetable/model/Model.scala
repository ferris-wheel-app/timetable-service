package com.ferris.timetable.model

import java.time.{LocalDate, LocalTime}
import java.util.UUID

object Model {

  case class Message (
    uuid: UUID,
    sender: String,
    content: String
  )

  sealed trait TimeBlock {
    def start: LocalTime
    def finish: LocalTime
  }

  case class ConcreteBlock (
    start: LocalTime,
    finish: LocalTime,
    task: Option[UUID]
  ) extends TimeBlock

  case class BufferBlock (
    start: LocalTime,
    finish: LocalTime,
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
}
