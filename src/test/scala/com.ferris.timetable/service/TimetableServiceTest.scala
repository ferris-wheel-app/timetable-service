package com.ferris.timetable.service

import java.util.UUID

import com.ferris.planning.MockPlanningServiceComponent
import com.ferris.timetable.db.{H2DatabaseComponent, H2TablesComponent}
import com.ferris.timetable.sample.SampleData.{domain => SD}
import com.ferris.timetable.service.exceptions.Exceptions._
import com.ferris.utils.MockTimerComponent
import org.mockito.Matchers.{eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures
import slick.dbio.DBIOAction

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class TimetableServiceTest extends FunSpec with ScalaFutures with Matchers {

  implicit val defaultTimeout: PatienceConfig = PatienceConfig(scaled(15.seconds))

  def newServer(bufferDuration: Int = 10) = new DefaultTimetableServiceComponent
    with MockTimetableRepositoryComponent
    with H2TablesComponent
    with H2DatabaseComponent
    with MockTimerComponent
    with MockPlanningServiceComponent {
    private val config = TimetableConfig(bufferDuration)
    override val timetableService: DefaultTimetableService = new DefaultTimetableService(config)
  }

  describe("a timetable service") {
    describe("handling messages") {
      it("should be able to create a message") {
        val server = newServer()
        when(server.repo.createMessage(SD.messageCreation)).thenReturn(DBIOAction.successful(SD.message))
        whenReady(server.timetableService.createMessage(SD.messageCreation)) { result =>
          result shouldBe SD.message
          verify(server.repo, times(1)).createMessage(SD.messageCreation)
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to update a message") {
        val server = newServer()
        val id = UUID.randomUUID
        val updated = SD.message
        when(server.repo.updateMessage(eqTo(id), eqTo(SD.messageUpdate))).thenReturn(DBIOAction.successful(updated))
        whenReady(server.timetableService.updateMessage(id, SD.messageUpdate)) { result =>
          result shouldBe updated
          verify(server.repo, times(1)).updateMessage(eqTo(id), eqTo(SD.messageUpdate))
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should return an error thrown by the repository when a message is being updated") {
        val server = newServer()
        val id = UUID.randomUUID
        val expectedException = MessageNotFoundException()
        when(server.repo.updateMessage(eqTo(id), eqTo(SD.messageUpdate))).thenReturn(DBIOAction.failed(expectedException))
        whenReady(server.timetableService.updateMessage(id, SD.messageUpdate).failed) { exception =>
          exception shouldBe expectedException
          verify(server.repo, times(1)).updateMessage(eqTo(id), eqTo(SD.messageUpdate))
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to retrieve a message") {
        val server = newServer()
        val id = UUID.randomUUID
        when(server.repo.getMessage(id)).thenReturn(DBIOAction.successful(Some(SD.message)))
        whenReady(server.timetableService.getMessage(id)) { result =>
          result shouldBe Some(SD.message)
          verify(server.repo, times(1)).getMessage(id)
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to retrieve all messages") {
        val server = newServer()
        val messages = Seq(SD.message, SD.message.copy(uuid = UUID.randomUUID))
        when(server.repo.getMessages).thenReturn(DBIOAction.successful(messages))
        whenReady(server.timetableService.getMessages) { result =>
          result shouldBe messages
          verify(server.repo, times(1)).getMessages
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to delete a message") {
        val server = newServer()
        val id = UUID.randomUUID
        when(server.repo.deleteMessage(id)).thenReturn(DBIOAction.successful(true))
        whenReady(server.timetableService.deleteMessage(id)) { result =>
          result shouldBe true
          verify(server.repo, times(1)).deleteMessage(id)
          verifyNoMoreInteractions(server.repo)
        }
      }
    }

    describe("handling routines") {
      it("should be able to create a routine") {
        val server = newServer()
        when(server.repo.createRoutine(SD.routineCreation)).thenReturn(DBIOAction.successful(SD.routine))
        whenReady(server.timetableService.createRoutine(SD.routineCreation)) { result =>
          result shouldBe SD.routine
          verify(server.repo, times(1)).createRoutine(SD.routineCreation)
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to update a routine") {
        val server = newServer()
        val id = UUID.randomUUID
        when(server.repo.updateRoutine(eqTo(id), eqTo(SD.routineUpdate))).thenReturn(DBIOAction.successful(true))
        whenReady(server.timetableService.updateRoutine(id, SD.routineUpdate)) { result =>
          result shouldBe true
          verify(server.repo, times(1)).updateRoutine(eqTo(id), eqTo(SD.routineUpdate))
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to start a routine") {
        val server = newServer()
        val id = UUID.randomUUID
        when(server.repo.startRoutine(eqTo(id))).thenReturn(DBIOAction.successful(true))
        whenReady(server.timetableService.startRoutine(id)) { result =>
          result shouldBe true
          verify(server.repo, times(1)).startRoutine(eqTo(id))
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to retrieve a routine") {
        val server = newServer()
        val id = UUID.randomUUID
        when(server.repo.getRoutine(id)).thenReturn(DBIOAction.successful(Some(SD.routine)))
        whenReady(server.timetableService.getRoutine(id)) { result =>
          result shouldBe Some(SD.routine)
          verify(server.repo, times(1)).getRoutine(id)
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to retrieve all routines") {
        val server = newServer()
        val routines = Seq(SD.routine, SD.routine.copy(uuid = UUID.randomUUID))
        when(server.repo.getRoutines).thenReturn(DBIOAction.successful(routines))
        whenReady(server.timetableService.getRoutines) { result =>
          result shouldBe routines
          verify(server.repo, times(1)).getRoutines
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to delete a routine") {
        val server = newServer()
        val id = UUID.randomUUID
        when(server.repo.deleteRoutine(id)).thenReturn(DBIOAction.successful(true))
        whenReady(server.timetableService.deleteRoutine(id)) { result =>
          result shouldBe true
          verify(server.repo, times(1)).deleteRoutine(id)
          verifyNoMoreInteractions(server.repo)
        }
      }
    }

    describe("handling timetables") {
      it("should be able to generate a timetable") {
        ???
      }

      it("should be able to update a timetable") {
        val server = newServer()
        when(server.repo.updateTimetable(eqTo(SD.timetableUpdate))).thenReturn(DBIOAction.successful(true))
        whenReady(server.timetableService.updateTimetable(SD.timetableUpdate)) { result =>
          result shouldBe true
          verify(server.repo, times(1)).updateTimetable(eqTo(SD.timetableUpdate))
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to retrieve the current timetable") {
        val server = newServer()
        when(server.repo.currentTimetable).thenReturn(DBIOAction.successful(Some(SD.timetable)))
        whenReady(server.timetableService.currentTimetable) { result =>
          result shouldBe Some(SD.timetable)
          verify(server.repo, times(1)).currentTimetable
          verifyNoMoreInteractions(server.repo)
        }
      }
    }
  }
}
