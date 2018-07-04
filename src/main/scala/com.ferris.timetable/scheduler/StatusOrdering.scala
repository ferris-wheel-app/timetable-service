package com.ferris.timetable.scheduler

import com.ferris.planning.model.Model.Statuses.{Complete, InProgress, Planned, Status}

object StatusOrdering extends Ordering[Status] {
  override def compare(x: Status, y: Status): Int = (x, y) match {
    case (Planned, Planned) | (InProgress, InProgress) | (Complete, Complete) => 0
    case (Planned, InProgress) | (InProgress, Complete) | (Planned, Complete) => -1
    case (InProgress, Planned) | (Complete, InProgress) | (Complete, Planned) => 1
  }
}
