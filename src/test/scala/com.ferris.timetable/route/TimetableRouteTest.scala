package com.ferris.timetable.route

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import com.ferris.microservice.exceptions.ApiExceptions._
import com.ferris.microservice.resource.{DeletionResult, UpdateResult}
import com.ferris.microservice.service.Envelope
import com.ferris.timetable.contract.resource.Resources.Out._
import com.ferris.timetable.service.conversions.ExternalToCommand._
import com.ferris.timetable.service.conversions.ModelToView._
import com.ferris.timetable.sample.SampleData.{domain, rest}
import com.ferris.timetable.service.exceptions.Exceptions.RoutineNotFoundException
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, verifyNoMoreInteractions, when}

import scala.concurrent.Future

class TimetableRouteTest extends RouteTestFramework {

  describe("a timetable API") {
    describe("handling messages") {
      describe("creating a message") {
        it("should respond with the created message") {
          when(testServer.timetableService.createMessage(eqTo(domain.messageCreation))(any())).thenReturn(Future.successful(domain.message))
          Post("/api/messages", rest.messageCreation) ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[MessageView]].data shouldBe rest.message
            verify(testServer.timetableService, times(1)).createMessage(eqTo(domain.messageCreation))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }

      describe("updating a message") {
        it("should respond with the updated message") {
          val id = UUID.randomUUID
          val update = rest.messageUpdate
          val updated = domain.message

          when(testServer.timetableService.updateMessage(eqTo(id), eqTo(update.toCommand))(any())).thenReturn(Future.successful(updated))
          Put(s"/api/messages/$id", update) ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[MessageView]].data shouldBe updated.toView
            verify(testServer.timetableService, times(1)).updateMessage(eqTo(id), eqTo(update.toCommand))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }

      describe("getting a message") {
        it("should respond with the requested message") {
          val id = UUID.randomUUID

          when(testServer.timetableService.getMessage(eqTo(id))(any())).thenReturn(Future.successful(Some(domain.message)))
          Get(s"/api/messages/$id") ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[MessageView]].data shouldBe rest.message
            verify(testServer.timetableService, times(1)).getMessage(eqTo(id))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }

        it("should respond with the appropriate error if the message is not found") {
          val id = UUID.randomUUID

          when(testServer.timetableService.getMessage(eqTo(id))(any())).thenReturn(Future.successful(None))
          Get(s"/api/messages/$id") ~> route ~> check {
            status shouldBe StatusCodes.NotFound
            verify(testServer.timetableService, times(1)).getMessage(eqTo(id))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }

      describe("getting messages") {
        it("should retrieve a list of all messages") {
          val messages = Seq(domain.message, domain.message.copy(uuid = UUID.randomUUID))

          when(testServer.timetableService.getMessages(any())).thenReturn(Future.successful(messages))
          Get(s"/api/messages") ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[Seq[MessageView]]].data shouldBe messages.map(_.toView)
            verify(testServer.timetableService, times(1)).getMessages(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }

      describe("deleting a message") {
        it("should return OK if the deletion is completed") {
          val id = UUID.randomUUID

          when(testServer.timetableService.deleteMessage(eqTo(id))(any())).thenReturn(Future.successful(true))
          Delete(s"/api/messages/$id") ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[DeletionResult]].data shouldBe DeletionResult.successful
            verify(testServer.timetableService, times(1)).deleteMessage(eqTo(id))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }

        it("should respond with the appropriate error if the deletion could not be completed") {
          val id = UUID.randomUUID

          when(testServer.timetableService.deleteMessage(eqTo(id))(any())).thenReturn(Future.successful(false))
          Delete(s"/api/messages/$id") ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[DeletionResult]].data shouldBe DeletionResult.unsuccessful
            verify(testServer.timetableService, times(1)).deleteMessage(eqTo(id))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }
    }

    describe("handling routines") {
      describe("creating a routine") {
        it("should respond with the created routine") {
          when(testServer.timetableService.createRoutine(eqTo(domain.routineCreation))(any())).thenReturn(Future.successful(domain.routine))
          Post("/api/routines", rest.routineCreation) ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[RoutineView]].data shouldBe rest.routine
            verify(testServer.timetableService, times(1)).createRoutine(eqTo(domain.routineCreation))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }

      describe("updating a routine") {
        it("should respond with OK if the routine gets updated") {
          val id = UUID.randomUUID
          val update = rest.routineUpdate

          when(testServer.timetableService.updateRoutine(eqTo(id), eqTo(update.toCommand))(any())).thenReturn(Future.successful(true))
          Put(s"/api/routines/$id", update) ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[UpdateResult]].data shouldBe UpdateResult.updated
            verify(testServer.timetableService, times(1)).updateRoutine(eqTo(id), eqTo(update.toCommand))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }

        it("should respond with NotModified if the routine does not get updated") {
          val id = UUID.randomUUID
          val update = rest.routineUpdate

          when(testServer.timetableService.updateRoutine(eqTo(id), eqTo(update.toCommand))(any())).thenReturn(Future.successful(false))
          Put(s"/api/routines/$id", update) ~> route ~> check {
            status shouldBe StatusCodes.NotModified
            responseAs[Envelope[UpdateResult]] shouldBe UpdateResult.notUpdated
            verify(testServer.timetableService, times(1)).updateRoutine(eqTo(id), eqTo(update.toCommand))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }

        it("should handle a RoutineNotFound exception appropriately") {
          val id = UUID.randomUUID
          val update = rest.routineUpdate

          when(testServer.timetableService.updateRoutine(eqTo(id), eqTo(update.toCommand))(any())).thenReturn(Future.failed(RoutineNotFoundException()))
          Put(s"/api/routines/$id", update) ~> route ~> check {
            status shouldBe StatusCodes.NotFound
            responseAs[Envelope[ExceptionList]].data.errors.head shouldBe NotFoundException("RoutineNotFound", "routine not found", Some(NotFoundPayload("uuid")))
            verify(testServer.timetableService, times(1)).updateRoutine(eqTo(id), eqTo(update.toCommand))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }
    }
  }
}





















































