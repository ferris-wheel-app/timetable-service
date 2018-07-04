package com.ferris.timetable.utils

import java.sql.Timestamp

import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

trait MockTimerComponent extends TimerComponent with MockitoSugar {
  override val timer: Timer = mock[Timer]
  when(timer.timestampOfNow).thenReturn(new Timestamp(System.currentTimeMillis()))
  when(timer.now).thenReturn(System.currentTimeMillis())
}
