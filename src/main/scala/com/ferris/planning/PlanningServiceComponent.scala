package com.ferris.planning

import com.ferris.planning.client.PlanningServiceClient

trait PlanningServiceComponent {
  def planningService: PlanningServiceClient
}
