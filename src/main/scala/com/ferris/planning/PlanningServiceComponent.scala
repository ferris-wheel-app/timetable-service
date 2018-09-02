package com.ferris.planning

import akka.http.scaladsl.model.Uri
import com.ferris.planning.client.PlanningServiceClient
import com.ferris.planning.server.PlanningServer
import com.typesafe.config.ConfigFactory

trait PlanningServiceComponent {
  def planningService: PlanningServiceClient
}

trait DefaultPlanningServiceComponent extends PlanningServiceComponent {
  override val planningService = new PlanningServiceClient(new PlanningServer(Uri(ConfigFactory.load.getString("profile-service.uri"))))
}
