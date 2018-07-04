package com.ferris.timetable.server

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import com.ferris.microservice.service.MicroServiceConfig
import com.ferris.planning.db.MySQLTablesComponent
import com.ferris.planning.repo.SqlPlanningRepositoryComponent
import com.ferris.planning.scheduler.DefaultLifeSchedulerComponent
import com.ferris.planning.service.DefaultPlanningServiceComponent
import com.ferris.planning.utils.DefaultTimerComponent

object PlanningMicroService extends PlanningServer
  with DefaultPlanningServiceComponent
  with SqlPlanningRepositoryComponent
  with MySQLTablesComponent
  with DefaultTimerComponent
  with DefaultLifeSchedulerComponent
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
