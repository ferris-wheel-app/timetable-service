package com.ferris.timetable.service

import com.ferris.planning.repo.PlanningRepositoryComponent
import org.scalatest.mockito.MockitoSugar.mock

trait MockPlanningRepositoryComponent extends PlanningRepositoryComponent {

  override val repo: PlanningRepository = mock[PlanningRepository]
}
