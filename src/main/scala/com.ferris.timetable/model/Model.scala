package com.ferris.timetable.model

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import com.ferris.timetable.model.Model.DaysOfTheWeek.DayOfTheWeek

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
    uuid: UUID,
    start: LocalDateTime,
    finish: LocalDateTime,
    task: Option[UUID]
  ) extends TimeBlock

  case class BufferBlock (
    uuid: UUID,
    start: LocalDateTime,
    finish: LocalDateTime,
    firstTask: Option[UUID],
    secondTask: Option[UUID]
  ) extends TimeBlock

  case class TimetableTemplate (
     uuid: UUID,
    day: DayOfTheWeek,
    blocks: Seq[TimeBlock]
  )

  case class Routine(
    uuid: UUID,
    name: String,
    templates: Seq[TimetableTemplate]
  )

  case class Timetable (
    date: LocalDate,
    blocks: Seq[TimeBlock]
  )

  trait TypeEnum {
    def dbValue: String
  }

  object DaysOfTheWeek {

    def withName(name: String): DayOfTheWeek = name match {
      case Monday.dbValue => Monday
      case Tuesday.dbValue => Tuesday
      case Wednesday.dbValue => Wednesday
      case Thursday.dbValue => Thursday
      case Friday.dbValue => Friday
      case Saturday.dbValue => Saturday
      case Sunday.dbValue => Sunday
    }

    sealed trait DayOfTheWeek extends TypeEnum

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
