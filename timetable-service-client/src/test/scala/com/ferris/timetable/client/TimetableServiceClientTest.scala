package com.ferris.timetable.client

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpResponse, RequestEntity, ResponseEntity}
import akka.stream.ActorMaterializer
import com.ferris.timetable.contract.format.TimetableRestFormats
import com.ferris.timetable.contract.resource.Resources.Out.DeletionResult
import com.ferris.timetable.contract.sample.{SampleData => SD}
import com.ferris.timetable.server.TimetableServer
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, Matchers}
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TimetableServiceClientTest extends FunSpec with Matchers with ScalaFutures with MockitoSugar with TimetableRestFormats {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  val mockServer: TimetableServer = mock[TimetableServer]
  val client = new TimetableServiceClient(mockServer)

  case class Envelope[T](status: String, data: T)

  implicit def envFormat[T](implicit ev: JsonFormat[T]): RootJsonFormat[Envelope[T]] = jsonFormat2(Envelope[T])

  describe("a timetable service client") {
    describe("handling creations") {
      it("should be able to create a message") {
        val creationRequest = Marshal(SD.messageCreation.toJson).to[RequestEntity].futureValue
        val creationResponse = Marshal(Envelope("OK", SD.message)).to[ResponseEntity].futureValue
        when(mockServer.sendPostRequest("/api/messages", creationRequest)).thenReturn(Future.successful(HttpResponse(entity = creationResponse)))
        whenReady(client.createMessage(SD.messageCreation)) { response =>
          response shouldBe SD.message
        }
      }
    }

    describe("handling updates") {
      it("should be able to update a message") {
        val id = UUID.randomUUID
        val updateRequest = Marshal(SD.messageUpdate.toJson).to[RequestEntity].futureValue
        val updateResponse = Marshal(Envelope("OK", SD.message)).to[ResponseEntity].futureValue
        when(mockServer.sendPutRequest(s"/api/messages/$id", updateRequest)).thenReturn(Future.successful(HttpResponse(entity = updateResponse)))
        whenReady(client.updateMessage(id, SD.messageUpdate)) { response =>
          response shouldBe SD.message
        }
      }
    }

    describe("handling retrievals") {
      it("should be able to retrieve a message") {
        val id = UUID.randomUUID
        val response = Marshal(Envelope("OK", SD.message)).to[ResponseEntity].futureValue
        when(mockServer.sendGetRequest(s"/api/messages/$id")).thenReturn(Future.successful(HttpResponse(entity = response)))
        whenReady(client.message(id)) { response =>
          response.get shouldBe SD.message
        }
      }

      it("should be able to retrieve a list of messages") {
        val list = SD.message :: SD.message :: Nil
        val response = Marshal(Envelope("OK", list)).to[ResponseEntity].futureValue
        when(mockServer.sendGetRequest("/api/messages")).thenReturn(Future.successful(HttpResponse(entity = response)))
        whenReady(client.messages) { response =>
          response shouldBe list
        }
      }
    }

    describe("handling deletions") {
      it("should be able to delete a message") {
        val id = UUID.randomUUID
        val response = Marshal(Envelope("OK", DeletionResult.successful)).to[ResponseEntity].futureValue
        when(mockServer.sendDeleteRequest(s"/api/messages/$id")).thenReturn(Future.successful(HttpResponse(entity = response)))
        whenReady(client.deleteMessage(id)) { response =>
          response shouldBe DeletionResult.successful
        }
      }
    }
  }
}
