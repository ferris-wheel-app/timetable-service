package com.ferris.timetable.utils

import java.sql.Timestamp

trait TimerComponent {
  def timer: Timer

  trait Timer {
    def now: Long
    def timestampOfNow: Timestamp
  }
}

trait DefaultTimerComponent extends TimerComponent {
  val timer: Timer = Timer

  private object Timer extends Timer {
    override def now = System.currentTimeMillis()
    override def timestampOfNow: Timestamp = new Timestamp(now)
  }
}
