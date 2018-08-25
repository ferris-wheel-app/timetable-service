package com.ferris.timetable.db.conversions

import java.sql.{Date, Timestamp}
import java.time.LocalDate
import java.util.UUID

import akka.http.scaladsl.model.DateTime
import com.ferris.timetable.table.Tables
import com.ferris.timetable.model.Model._

import scala.language.implicitConversions

class DomainConversions(val tables: Tables) {

  implicit class MessageBuilder(val row: tables.MessageRow) {
    def asMessage: Message = Message(
      uuid = UUID.fromString(row.uuid),
      sender = row.sender,
      content = row.content
    )
  }

  implicit class TimeBlockBuilder(val row: tables.TimeBlockRow) {
    def asTimeBlock: TimeBlockTemplate = TimeBlockTemplate(
      start = row.startTime.toLocalTime,
      finish = row.finishTime.toLocalTime,
      task = Task(
        uuid = row.taskId.map(UUID.fromString),
        `type` = TaskTypes.withName(row.taskType)
      )
    )
  }

  implicit class TimetableTemplateBuilder(val rows: Seq[tables.TimeBlockRow]) {
    def asTimetableTemplate: TimetableTemplate = TimetableTemplate(
      blocks = rows.map(_.asTimeBlock)
    )
  }

  implicit def uuid2String(uuid: UUID): String = uuid.toString

  implicit def uuid2String(uuid: Option[UUID]): Option[String] = uuid.map(_.toString)

  implicit def uuid2String(uuid: Seq[UUID]): Seq[String] = uuid.map(_.toString)

  implicit def timestamp2DateTime(date: Timestamp): DateTime = DateTime.apply(date.getTime)

  implicit def localDate2SqlDate(date: LocalDate): Date = Date.valueOf(date)

  implicit def byte2Boolean(byte: Byte): Boolean = byte == 1

  implicit def boolean2Byte(bool: Boolean): Byte = if (bool) 1 else 0
}
