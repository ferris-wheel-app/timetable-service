package com.ferris.timetable.service

import java.util.UUID

import com.ferris.timetable.db.{H2DatabaseComponent, H2TablesComponent}
import com.ferris.timetable.sample.SampleData.{domain => SD}
import com.ferris.timetable.service.exceptions.Exceptions._
import org.mockito.Matchers.{eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures
import slick.dbio.DBIOAction

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class TimetableServiceTest extends FunSpec with ScalaFutures with Matchers {

  implicit val defaultTimeout: PatienceConfig = PatienceConfig(scaled(15.seconds))

  def newServer = new DefaultTimetableServiceComponent
    with MockTimetableRepositoryComponent
    with H2TablesComponent
    with H2DatabaseComponent {
    override val timetableService: DefaultTimetableService = new DefaultTimetableService()
  }

  describe("a timetable service") {
    describe("handling messages") {
      it("should be able to create a message") {
        val server = newServer
        when(server.repo.createMessage(SD.messageCreation)).thenReturn(DBIOAction.successful(SD.message))
        whenReady(server.timetableService.createMessage(SD.messageCreation)) { result =>
          result shouldBe SD.message
          verify(server.repo, times(1)).createMessage(SD.messageCreation)
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to update a message") {
        val server = newServer
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
        val server = newServer
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
        val server = newServer
        val id = UUID.randomUUID
        when(server.repo.getMessage(id)).thenReturn(DBIOAction.successful(Some(SD.message)))
        whenReady(server.timetableService.getMessage(id)) { result =>
          result shouldBe Some(SD.message)
          verify(server.repo, times(1)).getMessage(id)
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to retrieve all messages") {
        val server = newServer
        val messages = Seq(SD.message, SD.message.copy(uuid = UUID.randomUUID))
        when(server.repo.getMessages).thenReturn(DBIOAction.successful(messages))
        whenReady(server.timetableService.getMessages) { result =>
          result shouldBe messages
          verify(server.repo, times(1)).getMessages
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to delete a message") {
        val server = newServer
        val id = UUID.randomUUID
        when(server.repo.deleteMessage(id)).thenReturn(DBIOAction.successful(true))
        whenReady(server.timetableService.deleteMessage(id)) { result =>
          result shouldBe true
          verify(server.repo, times(1)).deleteMessage(id)
          verifyNoMoreInteractions(server.repo)
        }
      }
    }
  }
}
