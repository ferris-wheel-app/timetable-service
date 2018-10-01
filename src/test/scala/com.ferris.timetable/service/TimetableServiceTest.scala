package com.ferris.timetable.service

import java.time.{LocalDate, LocalTime}
import java.util.UUID

import com.ferris.planning.MockPlanningServiceComponent
import com.ferris.timetable.db.{H2DatabaseComponent, H2TablesComponent}
import com.ferris.timetable.model.Model.TaskTypes
import com.ferris.timetable.sample.SampleData
import com.ferris.timetable.sample.SampleData.{domain => SD}
import com.ferris.timetable.service.conversions.TypeResolvers.TaskType
import com.ferris.timetable.service.exceptions.Exceptions._
import com.ferris.utils.MockTimerComponent
import org.mockito.Matchers.{eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures
import slick.dbio.DBIOAction

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
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

    describe("generating timetables") {
      it("should throw an exception if there is no current timetable template") {
        val server = newServer()

        when(server.repo.currentTemplate).thenReturn(DBIOAction.successful(None))
        whenReady(server.timetableService.generateTimetable.failed) { exception =>
          exception shouldBe CurrentTemplateNotFoundException()
          verify(server.repo).currentTemplate
          verifyZeroInteractions(server.planningService)
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should throw an exception if the current timetable template is invalid") {
        val server = newServer()
        val startTime = LocalTime.now
        val currentTemplate = SD.timetableTemplate.copy(
          blocks = SD.timeBlockTemplate.copy(
            start = startTime,
            finish = startTime.plusHours(1),
            task = SD.taskTemplate.copy(
              taskId = Some(UUID.randomUUID),
              `type` = TaskTypes.Thread
            )
          ) :: SD.timeBlockTemplate.copy(
            start = startTime.plusHours(1),
            finish = startTime.plusHours(2),
            task = SD.taskTemplate.copy(
              taskId = None,
              `type` = TaskTypes.Weave
            )
          ) :: SD.timeBlockTemplate.copy(
            start = startTime.plusHours(2),
            finish = startTime.plusHours(3),
            task = SD.taskTemplate.copy(
              taskId = None,
              `type` = TaskTypes.LaserDonut
            )
          ) :: SD.timeBlockTemplate.copy(
            start = startTime.plusHours(3),
            finish = startTime.plusHours(4),
            task = SD.taskTemplate.copy(
              taskId = Some(UUID.randomUUID),
              `type` = TaskTypes.LaserDonut
            )
          ) :: Nil
        )

        when(server.repo.currentTemplate).thenReturn(DBIOAction.successful(Some(currentTemplate)))
        whenReady(server.timetableService.generateTimetable.failed) { exception =>
          exception shouldBe InvalidTimetableException("there are time-blocks without specified tasks")
          verify(server.repo).currentTemplate
          verifyZeroInteractions(server.planningService)
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to generate a timetable") {
        val server = newServer()
        val today = LocalDate.now
        val startTime = LocalTime.now
        val threadId = UUID.randomUUID
        val weaveId = UUID.randomUUID
        val portionId = UUID.randomUUID
        val currentPortion = SampleData.rest.portion
        val currentTemplate = SD.timetableTemplate.copy(
          blocks = SD.timeBlockTemplate.copy(
            start = startTime,
            finish = startTime.plusHours(1),
            task = SD.taskTemplate.copy(
              taskId = Some(threadId),
              `type` = TaskTypes.Thread
            )
          ) :: SD.timeBlockTemplate.copy(
            start = startTime.plusHours(1),
            finish = startTime.plusHours(2),
            task = SD.taskTemplate.copy(
              taskId = Some(weaveId),
              `type` = TaskTypes.Weave
            )
          ) :: SD.timeBlockTemplate.copy(
            start = startTime.plusHours(2),
            finish = startTime.plusHours(3),
            task = SD.taskTemplate.copy(
              taskId = None,
              `type` = TaskTypes.LaserDonut
            )
          ) :: SD.timeBlockTemplate.copy(
            start = startTime.plusHours(3),
            finish = startTime.plusHours(4),
            task = SD.taskTemplate.copy(
              taskId = Some(portionId),
              `type` = TaskTypes.LaserDonut
            )
          ) :: Nil
        )
        val createCommand = SD.timetableCreation.copy(
          date = today,
          blocks = SD.scheduledTimeBlockCreation.copy(
            start = startTime,
            finish = startTime.plusHours(1),
            task = SD.scheduledTaskCreation.copy(
              taskId = threadId,
              `type` = TaskTypes.Thread
            )
          ) :: SD.scheduledTimeBlockCreation.copy(
            start = startTime.plusHours(1),
            finish = startTime.plusHours(2),
            task = SD.scheduledTaskCreation.copy(
              taskId = weaveId,
              `type` = TaskTypes.Weave
            )
          ) :: SD.scheduledTimeBlockCreation.copy(
            start = startTime.plusHours(2),
            finish = startTime.plusHours(3),
            task = SD.scheduledTaskCreation.copy(
              taskId = currentPortion.uuid,
              `type` = TaskTypes.LaserDonut
            )
          ) :: SD.scheduledTimeBlockCreation.copy(
            start = startTime.plusHours(3),
            finish = startTime.plusHours(4),
            task = SD.scheduledTaskCreation.copy(
              taskId = portionId,
              `type` = TaskTypes.LaserDonut
            )
          ) :: Nil
        )
        val concreteBlock1 = SD.concreteBlock.copy(
          start = startTime,
          finish = startTime.plusHours(1),
          task = SD.scheduledTask.copy(
            taskId = threadId,
            `type` = TaskTypes.Thread
          )
        )
        val bufferBlock1 = SD.bufferBlock.copy(
          start = startTime.plusHours(1).minusMinutes(5),
          finish = startTime.plusHours(1).plusMinutes(5),
          firstTask = SD.scheduledTask.copy(
            taskId = threadId,
            `type` = TaskTypes.Thread
          ),
          secondTask = SD.scheduledTask.copy(
            taskId = weaveId,
            `type` = TaskTypes.Weave
          )
        )
        val concreteBlock2 = SD.concreteBlock.copy(
          start = startTime.plusHours(1),
          finish = startTime.plusHours(2),
          task = SD.scheduledTask.copy(
            taskId = weaveId,
            `type` = TaskTypes.Weave
          )
        )
        val bufferBlock2 = SD.bufferBlock.copy(
          start = startTime.plusHours(2).minusMinutes(5),
          finish = startTime.plusHours(2).plusMinutes(5),
          firstTask = SD.scheduledTask.copy(
            taskId = weaveId,
            `type` = TaskTypes.Weave
          ),
          secondTask = SD.scheduledTask.copy(
            taskId = currentPortion.uuid,
            `type` = TaskTypes.LaserDonut
          )
        )
        val concreteBlock3 = SD.concreteBlock.copy(
          start = startTime.plusHours(2),
          finish = startTime.plusHours(3),
          task = SD.scheduledTask.copy(
            taskId = currentPortion.uuid,
            `type` = TaskTypes.LaserDonut
          )
        )
        val bufferBlock3 = SD.bufferBlock.copy(
          start = startTime.plusHours(3).minusMinutes(5),
          finish = startTime.plusHours(3).plusMinutes(5),
          firstTask = SD.scheduledTask.copy(
            taskId = currentPortion.uuid,
            `type` = TaskTypes.LaserDonut
          ),
          secondTask = SD.scheduledTask.copy(
            taskId = portionId,
            `type` = TaskTypes.LaserDonut
          )
        )
        val concreteBlock4 = SD.concreteBlock.copy(
          start = startTime.plusHours(3),
          finish = startTime.plusHours(4),
          task = SD.scheduledTask.copy(
            taskId = portionId,
            `type` = TaskTypes.LaserDonut
          )
        )
        val timetableInRepo = SD.timetable.copy(
          date = today,
          blocks = concreteBlock1 :: concreteBlock2 :: concreteBlock3 :: concreteBlock4 :: Nil
        )
        val summary = "Write tests for the input validation"
        val timetableView = SampleData.rest.timetable.copy(
          date = today,
          blocks = SampleData.rest.concreteBlock.copy(
            start = concreteBlock1.start,
            finish = concreteBlock1.finish,
            task = SampleData.rest.scheduledTask.copy(
              taskId = concreteBlock1.task.taskId,
              `type` = TaskType.toString(concreteBlock1.task.`type`),
              summary = Some(SampleData.rest.thread.summary),
              isDone = false
            )
          ) :: SampleData.rest.bufferBlock.copy(
            start = bufferBlock1.start,
            finish = bufferBlock1.finish,
            firstTask = SampleData.rest.scheduledTask.copy(
              taskId = bufferBlock1.firstTask.taskId,
              `type` = TaskType.toString(bufferBlock1.firstTask.`type`),
              summary = Some(SampleData.rest.thread.summary),
              isDone = false
            ),
            secondTask = SampleData.rest.scheduledTask.copy(
              taskId = bufferBlock1.secondTask.taskId,
              `type` = TaskType.toString(bufferBlock1.secondTask.`type`),
              summary = Some(SampleData.rest.weave.summary),
              isDone = false
            )
          ) :: SampleData.rest.concreteBlock.copy(
            start = concreteBlock2.start,
            finish = concreteBlock2.finish,
            task = SampleData.rest.scheduledTask.copy(
              taskId = concreteBlock2.task.taskId,
              `type` = TaskType.toString(concreteBlock2.task.`type`),
              summary = Some(SampleData.rest.weave.summary),
              isDone = false
            )
          ) :: SampleData.rest.bufferBlock.copy(
            start = bufferBlock2.start,
            finish = bufferBlock2.finish,
            firstTask = SampleData.rest.scheduledTask.copy(
              taskId = bufferBlock2.firstTask.taskId,
              `type` = TaskType.toString(bufferBlock2.firstTask.`type`),
              summary = Some(SampleData.rest.weave.summary),
              isDone = false
            ),
            secondTask = SampleData.rest.scheduledTask.copy(
              taskId = bufferBlock2.secondTask.taskId,
              `type` = TaskType.toString(bufferBlock2.secondTask.`type`),
              summary = Some(summary),
              isDone = false
            )
          ) :: SampleData.rest.concreteBlock.copy(
            start = concreteBlock3.start,
            finish = concreteBlock3.finish,
            task = SampleData.rest.scheduledTask.copy(
              taskId = concreteBlock3.task.taskId,
              `type` = TaskType.toString(concreteBlock3.task.`type`),
              summary = Some(summary),
              isDone = false
            )
          ) :: SampleData.rest.bufferBlock.copy(
            start = bufferBlock3.start,
            finish = bufferBlock3.finish,
            firstTask = SampleData.rest.scheduledTask.copy(
              taskId = bufferBlock3.firstTask.taskId,
              `type` = TaskType.toString(bufferBlock3.firstTask.`type`),
              summary = Some(summary),
              isDone = false
            ),
            secondTask = SampleData.rest.scheduledTask.copy(
              taskId = bufferBlock3.secondTask.taskId,
              `type` = TaskType.toString(bufferBlock3.secondTask.`type`),
              summary = Some(SampleData.rest.portion.summary),
              isDone = false
            )
          ) :: SampleData.rest.concreteBlock.copy(
            start = concreteBlock4.start,
            finish = concreteBlock4.finish,
            task = SampleData.rest.scheduledTask.copy(
              taskId = concreteBlock4.task.taskId,
              `type` = TaskType.toString(concreteBlock4.task.`type`),
              summary = Some(SampleData.rest.portion.summary),
              isDone = false
            )
          ) :: Nil
        )

        when(server.repo.currentTemplate).thenReturn(DBIOAction.successful(Some(currentTemplate)))
        when(server.timer.today).thenReturn(today)
        when(server.planningService.currentPortion).thenReturn(Future.successful(Some(currentPortion)))
        when(server.repo.createTimetable(createCommand)).thenReturn(DBIOAction.successful(timetableInRepo))
        when(server.planningService.thread(threadId)).thenReturn(Future.successful(Some(SampleData.rest.thread)))
        when(server.planningService.weave(weaveId)).thenReturn(Future.successful(Some(SampleData.rest.weave)))
        when(server.planningService.portion(currentPortion.uuid)).thenReturn(Future.successful(Some(SampleData.rest.portion.copy(summary = summary))))
        when(server.planningService.portion(portionId)).thenReturn(Future.successful(Some(SampleData.rest.portion)))
        whenReady(server.timetableService.generateTimetable) { result =>
          result shouldBe timetableView
          verify(server.repo).currentTemplate
          verify(server.planningService).currentPortion
          verify(server.repo).createTimetable(createCommand)
          verify(server.planningService, times(2)).thread(threadId)
          verify(server.planningService, times(3)).weave(weaveId)
          verify(server.planningService, times(3)).portion(currentPortion.uuid)
          verify(server.planningService, times(2)).portion(portionId)
        }
      }
    }

    describe("handling timetables") {
      it("should be able to update a timetable") {
        val server = newServer()
        when(server.repo.updateTimetable(eqTo(SD.timetableUpdate))).thenReturn(DBIOAction.successful(true))
        whenReady(server.timetableService.updateCurrentTimetable(SD.timetableUpdate)) { result =>
          result shouldBe true
          verify(server.repo).updateTimetable(eqTo(SD.timetableUpdate))
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to retrieve the current timetable") {
        val server = newServer()
        when(server.repo.currentTimetable).thenReturn(DBIOAction.successful(Some(SD.timetable)))
        whenReady(server.timetableService.currentTimetable) { result =>
          result shouldBe Some(SD.timetable)
          verify(server.repo).currentTimetable
          verifyNoMoreInteractions(server.repo)
        }
      }
    }
  }
}
