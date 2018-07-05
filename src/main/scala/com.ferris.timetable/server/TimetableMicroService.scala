package com.ferris.timetable.server

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import com.ferris.microservice.service.MicroServiceConfig
import com.ferris.timetable.db.MySQLTablesComponent
import com.ferris.timetable.repo.SqlTimetableRepositoryComponent
import com.ferris.timetable.service.DefaultTimetableServiceComponent
import com.ferris.timetable.utils.DefaultTimerComponent

object TimetableMicroService extends TimetableServer
  with DefaultTimetableServiceComponent
  with SqlTimetableRepositoryComponent
  with MySQLTablesComponent
  with DefaultTimerComponent
  with App {
  override implicit lazy val system = ActorSystem()
  override implicit lazy val executor = system.dispatcher
  override implicit lazy val materializer = ActorMaterializer()
  override implicit val repoEc = scala.concurrent.ExecutionContext.global
  override implicit val routeEc = scala.concurrent.ExecutionContext.global

  override val logger = Logging(system, getClass)
  override val config = MicroServiceConfig

  val db = tables.profile.api.Database.forConfig("db")

  startUp()
}
