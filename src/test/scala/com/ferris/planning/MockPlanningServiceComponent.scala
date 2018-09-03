package com.ferris.planning

import com.ferris.planning.client.PlanningServiceClient
import org.scalatest.mockito.MockitoSugar.mock

trait MockPlanningServiceComponent extends PlanningServiceComponent {
  override val planningService: PlanningServiceClient = mock[PlanningServiceClient]
}
