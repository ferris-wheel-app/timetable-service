package com.ferris.timetable.utils

import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneId}

object PlanningImplicits {
  implicit class LocalDateTimeOps(time: LocalDateTime) {
    def toLong: Long = {
      time.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli
    }

    def toTimestamp: Timestamp = {
      new Timestamp(time.toLong)
    }
  }
}
