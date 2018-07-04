package com.ferris.timetable.route

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import com.ferris.microservice.exceptions.ApiExceptionFormats
import com.ferris.microservice.service.{Envelope, MicroServiceConfig}
import com.ferris.planning.contract.format.PlanningRestFormats
import com.ferris.planning.server.PlanningServer
import com.ferris.planning.service.PlanningServiceComponent
import org.scalatest.{FunSpec, Matchers, Outcome}
import org.scalatest.mockito.MockitoSugar.mock
import spray.json._

trait MockPlanningServiceComponent extends PlanningServiceComponent {
  override val planningService: PlanningService = mock[PlanningService]
}

trait RouteTestFramework extends FunSpec with ScalatestRouteTest with PlanningRestFormats with ApiExceptionFormats with Matchers {

  var testServer: PlanningServer with PlanningServiceComponent = _
  var route: Route = _

  implicit def envFormat[T](implicit ev: JsonFormat[T]): RootJsonFormat[Envelope[T]] = jsonFormat2(Envelope[T])

  override def withFixture(test: NoArgTest): Outcome = {
    testServer = new PlanningServer with MockPlanningServiceComponent {
      override implicit lazy val system = ActorSystem()

      override implicit lazy val executor = system.dispatcher

      override implicit lazy val materializer = ActorMaterializer()

      override implicit val routeEc = scala.concurrent.ExecutionContext.global

      override val config = MicroServiceConfig

      override val logger: LoggingAdapter = Logging(system, getClass)
    }

    route = testServer.route

    super.withFixture(test)
  }
}
