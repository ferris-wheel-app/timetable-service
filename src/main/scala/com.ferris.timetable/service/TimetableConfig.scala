package com.ferris.timetable.service

import com.typesafe.config.{Config, ConfigFactory}

case class TimetableConfig(bufferDuration: Int) {
  require(bufferDuration >= 0 && bufferDuration <= 30)
}

object TimetableConfig {
  def apply(config: Config): TimetableConfig = {
    TimetableConfig(config.getInt("timetable-service.env.timetable-settings.buffer-duration"))
  }
}

object DefaultTimetableConfig {
  def apply: TimetableConfig = TimetableConfig(ConfigFactory.load())
}
