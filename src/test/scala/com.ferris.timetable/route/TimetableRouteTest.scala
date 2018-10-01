package com.ferris.timetable.route

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import com.ferris.microservice.resource.DeletionResult
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
            verify(testServer.timetableService, times(1)).updateRoutine(eqTo(id), eqTo(update.toCommand))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }

      describe("starting a routine") {
        it("should respond with OK if the routine gets started") {
          val id = UUID.randomUUID

          when(testServer.timetableService.startRoutine(eqTo(id))(any())).thenReturn(Future.successful(true))
          Put(s"/api/routines/$id/start") ~> route ~> check {
            status shouldBe StatusCodes.OK
            verify(testServer.timetableService, times(1)).startRoutine(eqTo(id))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }

        it("should respond with NotModified if the routine does not get started") {
          val id = UUID.randomUUID

          when(testServer.timetableService.startRoutine(eqTo(id))(any())).thenReturn(Future.successful(false))
          Put(s"/api/routines/$id/start") ~> route ~> check {
            status shouldBe StatusCodes.NotModified
            verify(testServer.timetableService, times(1)).startRoutine(eqTo(id))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }

        it("should handle a RoutineNotFound exception appropriately") {
          val id = UUID.randomUUID
          val update = rest.routineUpdate

          when(testServer.timetableService.updateRoutine(eqTo(id), eqTo(update.toCommand))(any())).thenReturn(Future.failed(RoutineNotFoundException()))
          Put(s"/api/routines/$id", update) ~> route ~> check {
            status shouldBe StatusCodes.NotFound
            verify(testServer.timetableService, times(1)).updateRoutine(eqTo(id), eqTo(update.toCommand))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }

      describe("retrieving a routine") {
        it("should respond with the requested routine") {
          val id = UUID.randomUUID

          when(testServer.timetableService.getRoutine(eqTo(id))(any())).thenReturn(Future.successful(Some(domain.routine)))
          Get(s"/api/routines/$id") ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[RoutineView]].data shouldBe rest.routine
            verify(testServer.timetableService, times(1)).getRoutine(eqTo(id))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }

        it("should respond with the appropriate error if the routine is not found") {
          val id = UUID.randomUUID

          when(testServer.timetableService.getRoutine(eqTo(id))(any())).thenReturn(Future.successful(None))
          Get(s"/api/routines/$id") ~> route ~> check {
            status shouldBe StatusCodes.NotFound
            verify(testServer.timetableService, times(1)).getRoutine(eqTo(id))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }

      describe("retrieving routines") {
        it("should retrieve a list of all messages") {
          val routines = Seq(domain.routine, domain.routine.copy(uuid = UUID.randomUUID))

          when(testServer.timetableService.getRoutines(any())).thenReturn(Future.successful(routines))
          Get(s"/api/routines") ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[Seq[RoutineView]]].data shouldBe routines.map(_.toView)
            verify(testServer.timetableService, times(1)).getRoutines(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }

      describe("deleting a routine") {
        it("should return OK if the deletion is completed") {
          val id = UUID.randomUUID

          when(testServer.timetableService.deleteRoutine(eqTo(id))(any())).thenReturn(Future.successful(true))
          Delete(s"/api/routines/$id") ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[DeletionResult]].data shouldBe DeletionResult.successful
            verify(testServer.timetableService, times(1)).deleteRoutine(eqTo(id))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }

        it("should respond with the appropriate error if the deletion could not be completed") {
          val id = UUID.randomUUID

          when(testServer.timetableService.deleteRoutine(eqTo(id))(any())).thenReturn(Future.successful(false))
          Delete(s"/api/routines/$id") ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[DeletionResult]].data shouldBe DeletionResult.unsuccessful
            verify(testServer.timetableService, times(1)).deleteRoutine(eqTo(id))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }
    }

    describe("handling timetables") {
      describe("generating a timetable") {
        it("should respond with the generated timetable") {
          when(testServer.timetableService.generateTimetable(any())).thenReturn(Future.successful(rest.timetable))
          Post("/api/timetables/generate") ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[TimetableView]].data shouldBe rest.timetable
            verify(testServer.timetableService, times(1)).generateTimetable(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }

      describe("updating a timetable") {
        it("should respond with OK if the timetable gets updated") {
          val update = rest.timetableUpdate

          when(testServer.timetableService.updateCurrentTimetable(eqTo(update.toCommand))(any())).thenReturn(Future.successful(true))
          Put("/api/timetables/current", update) ~> route ~> check {
            status shouldBe StatusCodes.OK
            verify(testServer.timetableService, times(1)).updateCurrentTimetable(eqTo(update.toCommand))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }

        it("should respond with NotModified if the timetable does not get updated") {
          val update = rest.timetableUpdate

          when(testServer.timetableService.updateCurrentTimetable(eqTo(update.toCommand))(any())).thenReturn(Future.successful(false))
          Put("/api/timetables/current", update) ~> route ~> check {
            status shouldBe StatusCodes.NotModified
            verify(testServer.timetableService, times(1)).updateCurrentTimetable(eqTo(update.toCommand))(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }

      describe("getting the current timetable") {
        it("should respond with the current timetable") {
          when(testServer.timetableService.currentTimetable(any())).thenReturn(Future.successful(Some(domain.timetable)))
          Get("/api/timetables/current") ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Envelope[TimetableView]].data shouldBe rest.timetable
            verify(testServer.timetableService, times(1)).currentTimetable(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }

        it("should respond with the appropriate error if the timetable is not found") {
          when(testServer.timetableService.currentTimetable(any())).thenReturn(Future.successful(None))
          Get("/api/timetables/current") ~> route ~> check {
            status shouldBe StatusCodes.NotFound
            verify(testServer.timetableService, times(1)).currentTimetable(any())
            verifyNoMoreInteractions(testServer.timetableService)
          }
        }
      }
    }
  }
}
