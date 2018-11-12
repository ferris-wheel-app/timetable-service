package com.ferris.timetable.contract.resource

object TypeFields {

  object TaskType {
    val thread = "thread"
    val weave = "weave"
    val laserDonut = "laser_donut"
    val hobby = "hobby"
    val oneOff = "oneOff"
    val scheduledOneOff = "scheduledOneOff"
    val values = Set(thread, weave, laserDonut, hobby, oneOff, scheduledOneOff)
  }
}
