package com.ferris.timetable.server

import akka.http.scaladsl.server.Route
import com.ferris.microservice.service.MicroService
import com.ferris.planning.route.PlanningRoute
import com.ferris.planning.service.PlanningServiceComponent

abstract class PlanningServer extends MicroService with PlanningRoute {
  this: PlanningServiceComponent =>

  def route: Route = api(
    externalRoutes = handleExceptions(PlanningExceptionHandler.handler) { planningRoute }
  )
}
