package com.ferris.timetable.utils

import java.time.DayOfWeek
import java.util.concurrent.TimeUnit

import com.ferris.timetable.model.Model.DayOfTheWeek
import com.ferris.utils.TimerComponent

trait TimetableUtils extends TimerComponent {
  def dayOfTheWeek: DayOfTheWeek = timer.today.getDayOfWeek match {
    case DayOfWeek.MONDAY => DayOfTheWeek.Monday
    case DayOfWeek.TUESDAY => DayOfTheWeek.Tuesday
    case DayOfWeek.WEDNESDAY => DayOfTheWeek.Wednesday
    case DayOfWeek.THURSDAY => DayOfTheWeek.Thursday
    case DayOfWeek.FRIDAY => DayOfTheWeek.Friday
    case DayOfWeek.SATURDAY => DayOfTheWeek.Saturday
    case DayOfWeek.SUNDAY => DayOfTheWeek.Sunday
  }

  def todayIsWeekend: Boolean = {
    List(DayOfTheWeek.Saturday, DayOfTheWeek.Sunday).contains(dayOfTheWeek)
  }

  def getDurationHms(millis: Long): String = {
    String.format("%02d:%02d",
      TimeUnit.MILLISECONDS.toHours(millis).asInstanceOf[java.lang.Object],
      (TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))).asInstanceOf[java.lang.Object])
  }
}
