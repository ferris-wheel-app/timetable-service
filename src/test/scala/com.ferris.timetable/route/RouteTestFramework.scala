package com.ferris.timetable.route

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import com.ferris.microservice.exceptions.ApiExceptionFormats
import com.ferris.microservice.service.{Envelope, MicroServiceConfig}
import com.ferris.timetable.contract.format.TimetableRestFormats
import com.ferris.timetable.server.TimetableServer
import com.ferris.timetable.service.TimetableServiceComponent
import org.scalatest.{FunSpec, Matchers, Outcome}
import org.scalatest.mockito.MockitoSugar.mock
import spray.json._

trait MockTimetableServiceComponent extends TimetableServiceComponent {
  override val timetableService: TimetableService = mock[TimetableService]
}

trait RouteTestFramework extends FunSpec with ScalatestRouteTest with TimetableRestFormats with ApiExceptionFormats with Matchers {

  var testServer: TimetableServer with TimetableServiceComponent = _
  var route: Route = _

  implicit def envFormat[T](implicit ev: JsonFormat[T]): RootJsonFormat[Envelope[T]] = jsonFormat2(Envelope[T])

  override def withFixture(test: NoArgTest): Outcome = {
    testServer = new TimetableServer with MockTimetableServiceComponent {
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
