package com.ferris.timetable.service

import com.ferris.timetable.repo.TimetableRepositoryComponent
import org.scalatest.mockito.MockitoSugar.mock

trait MockTimetableRepositoryComponent extends TimetableRepositoryComponent {

  override val repo: TimetableRepository = mock[TimetableRepository]
}
