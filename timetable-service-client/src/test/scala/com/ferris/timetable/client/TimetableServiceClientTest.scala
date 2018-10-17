package com.ferris.timetable.client

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.ferris.timetable.contract.format.TimetableRestFormats
import com.ferris.timetable.server.TimetableServer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, Matchers}
import spray.json._

class TimetableServiceClientTest extends FunSpec with Matchers with ScalaFutures with MockitoSugar with TimetableRestFormats {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  val mockServer: TimetableServer = mock[TimetableServer]
  val client = new TimetableServiceClient(mockServer)

  case class Envelope[T](status: String, data: T)

  implicit def envFormat[T](implicit ev: JsonFormat[T]): RootJsonFormat[Envelope[T]] = jsonFormat2(Envelope[T])
}
