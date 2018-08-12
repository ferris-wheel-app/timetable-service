package com.ferris.timetable.repo

import com.ferris.timetable.db.{H2DatabaseComponent, H2TablesComponent}
import com.ferris.utils.TimerComponent

import scala.concurrent.ExecutionContext

trait H2TimetableRepositoryComponent extends SqlTimetableRepositoryComponent
  with H2TablesComponent
  with H2DatabaseComponent
  with TimerComponent {

  override implicit val repoEc: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override val repo = new SqlTimetableRepository
}
