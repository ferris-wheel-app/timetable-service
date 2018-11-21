package com.ferris.timetable.server

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import com.ferris.microservice.service.MicroServiceConfig
import com.ferris.planning.PlanningServiceComponent
import com.ferris.planning.client.PlanningServiceClient
import com.ferris.planning.server.PlanningServer
import com.ferris.timetable.db.{DatabaseComponent, MySQLTablesComponent}
import com.ferris.timetable.repo.SqlTimetableRepositoryComponent
import com.ferris.timetable.service.DefaultTimetableServiceComponent
import com.ferris.timetable.utils.TimetableUtils
import com.ferris.utils.DefaultTimerComponent
import com.typesafe.config.ConfigFactory

object TimetableMicroService extends TimetableServer
  with DefaultTimetableServiceComponent
  with SqlTimetableRepositoryComponent
  with DatabaseComponent
  with MySQLTablesComponent
  with DefaultTimerComponent
  with PlanningServiceComponent
  with TimetableUtils
  with App {
  override implicit lazy val system = ActorSystem()
  override implicit lazy val executor = system.dispatcher
  override implicit lazy val materializer = ActorMaterializer()

  override implicit val repoEc = scala.concurrent.ExecutionContext.global
  override implicit val routeEc = scala.concurrent.ExecutionContext.global

  override val logger = Logging(system, getClass)
  override val config = MicroServiceConfig

  override val db = tables.profile.api.Database.forConfig("timetable-service.env.db")
  override val planningService = new PlanningServiceClient(new PlanningServer(
    Uri(ConfigFactory.load.getString("timetable-service.env.planning-service-client.url")))(materializer, system), materializer)

  startUp()
}
