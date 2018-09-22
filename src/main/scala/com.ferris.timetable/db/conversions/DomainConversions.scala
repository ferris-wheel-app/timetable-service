package com.ferris.timetable.db.conversions

import java.sql.Timestamp
import java.time.LocalDate
import java.util.UUID

import akka.http.scaladsl.model.DateTime
import com.ferris.timetable.db.Tables
import com.ferris.timetable.model.Model._
import com.ferris.utils.FerrisImplicits._

import scala.language.implicitConversions

class DomainConversions(val tables: Tables) {

  implicit class MessageBuilder(val row: tables.MessageRow) {
    def asMessage: Message = Message(
      uuid = UUID.fromString(row.uuid),
      sender = row.sender,
      content = row.content
    )
  }

  implicit class TimeBlockTemplateBuilder(val row: tables.TimeBlockRow) {
    def asTimeBlockTemplate: TimeBlockTemplate = TimeBlockTemplate(
      start = row.startTime.toLocalTime,
      finish = row.finishTime.toLocalTime,
      task = TaskTemplate(
        taskId = row.taskId.map(UUID.fromString),
        `type` = TaskTypes.withName(row.taskType)
      )
    )
  }

  implicit class TimetableTemplateBuilder(val rows: Seq[tables.TimeBlockRow]) {
    def asTimetableTemplate: TimetableTemplate = TimetableTemplate(
      blocks = rows.map(_.asTimeBlockTemplate)
    )
  }

  implicit class ScheduledTimeBlockBuilder(val row: tables.ScheduledTimeBlockRow) {
    def asScheduledTimeBlock: ScheduledTimeBlock = ConcreteBlock(
      start = row.startTime.toLocalTime,
      finish = row.finishTime.toLocalTime,
      task = ScheduledTask(
        taskId = UUID.fromString(row.taskId),
        `type` = TaskTypes.withName(row.taskType),
        isDone = row.isDone
      )
    )
  }

  implicit class TimetableBuilder(val rows: Seq[tables.ScheduledTimeBlockRow]) {
    def asTimetable(date: LocalDate): Timetable = Timetable(
      date = date,
      blocks = rows.map(_.asScheduledTimeBlock)
    )
  }

  implicit def timestamp2DateTime(date: Timestamp): DateTime = DateTime.apply(date.getTime)
}
