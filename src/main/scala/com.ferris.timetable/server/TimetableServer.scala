package com.ferris.timetable.server

import akka.http.scaladsl.server.Route
import com.ferris.microservice.service.MicroService
import com.ferris.timetable.route.TimetableRoute
import com.ferris.timetable.service.TimetableServiceComponent

abstract class TimetableServer extends MicroService with TimetableRoute {
  this: TimetableServiceComponent =>

  def route: Route = api(
    externalRoutes = handleExceptions(TimetableExceptionHandler.handler) { timetableRoute }
  )
}
