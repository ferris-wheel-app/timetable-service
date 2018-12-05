package com.ferris.timetable.service

import java.time.{LocalDate, LocalTime}
import java.util.UUID

import com.ferris.planning.MockPlanningServiceComponent
import com.ferris.planning.contract.resource.Resources.Out.{OneOffView, PortionView, ScheduledOneOffView}
import com.ferris.planning.contract.resource.TypeFields.Status
import com.ferris.timetable.command.Commands.CreateTimetable
import com.ferris.timetable.contract.resource.Resources.Out.TimetableView
import com.ferris.timetable.db.{H2TablesComponent, MockDatabaseComponent}
import com.ferris.timetable.model.Model.{TaskTypes, Timetable, TimetableTemplate}
import com.ferris.timetable.sample.SampleData
import com.ferris.timetable.sample.SampleData.{domain => SD}
import com.ferris.timetable.service.conversions.TypeResolvers.TaskType
import com.ferris.timetable.service.exceptions.Exceptions._
import com.ferris.timetable.utils.TimetableUtils
import com.ferris.utils.MockTimerComponent
import org.mockito.Matchers.{eq => eqTo, any}
import org.mockito.Mockito._
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures
import slick.dbio.DBIOAction

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class TimetableServiceTest extends FunSpec with ScalaFutures with Matchers {

  implicit val defaultTimeout: PatienceConfig = PatienceConfig(scaled(15.seconds))

  type Server = DefaultTimetableServiceComponent
    with MockTimetableRepositoryComponent
    with H2TablesComponent
    with MockDatabaseComponent
    with MockTimerComponent
    with MockPlanningServiceComponent
    with TimetableUtils

  def newServer(bufferDuration: Int = 10) = new DefaultTimetableServiceComponent
    with MockTimetableRepositoryComponent
    with H2TablesComponent
    with MockDatabaseComponent
    with MockTimerComponent
    with MockPlanningServiceComponent
    with TimetableUtils {
    private val config = TimetableConfig(bufferDuration)
    override val timetableService: DefaultTimetableService = new DefaultTimetableService(config)
  }

  describe("a timetable service") {
    describe("handling routines") {
      it("should be able to create a routine") {
        val server = newServer()
        when(server.repo.createRoutine(SD.routineCreation)).thenReturn(DBIOAction.successful(SD.routine))
        when(server.db.run(DBIOAction.successful(SD.routine))).thenReturn(Future.successful(SD.routine))
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
        when(server.db.run(DBIOAction.successful(true))).thenReturn(Future.successful(true))
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
        when(server.db.run(DBIOAction.successful(true))).thenReturn(Future.successful(true))
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
        when(server.db.run(DBIOAction.successful(Some(SD.routine)))).thenReturn(Future.successful(Some(SD.routine)))
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
        when(server.db.run(DBIOAction.successful(routines))).thenReturn(Future.successful(routines))
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
        when(server.db.run(DBIOAction.successful(true))).thenReturn(Future.successful(true))
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
        when(server.db.run(DBIOAction.successful(None))).thenReturn(Future.successful(None))
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
        when(server.db.run(DBIOAction.successful(Some(currentTemplate)))).thenReturn(Future.successful(Some(currentTemplate)))
        whenReady(server.timetableService.generateTimetable.failed) { exception =>
          exception shouldBe InvalidTimetableException("there are time-blocks without specified tasks")
          verify(server.repo).currentTemplate
          verifyZeroInteractions(server.planningService)
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to generate a timetable if all criteria are met") {
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

        testTimetableGeneration(
          today = today,
          threadId = threadId,
          weaveId = weaveId,
          portionId = portionId,
          currentPortion = currentPortion,
          portionSummary = summary
        )(
          currentTemplate = currentTemplate,
          createCommand = createCommand,
          timetableInRepo = timetableInRepo,
          timetableView = timetableView
        ){ server =>
          verify(server.repo).currentTemplate
          verify(server.planningService).currentPortion
          verify(server.repo).createTimetable(createCommand)
          verify(server.planningService, times(2)).thread(threadId)
          verify(server.planningService, times(3)).weave(weaveId)
          verify(server.planningService, times(3)).portion(currentPortion.uuid)
          verify(server.planningService, times(2)).portion(portionId)
        }
      }

      describe("with one-offs") {
        val today = LocalDate.now
        val startTime = LocalTime.of(9, 30)
        val threadId = UUID.randomUUID
        val weaveId = UUID.randomUUID

        it("should correctly handle a one-off having a bigger estimate than the slot") {
          val thread = SD.timeBlockTemplate.copy(
            start = startTime,
            finish = startTime.plusHours(2),
            task = SD.taskTemplate.copy(
              taskId = Some(threadId),
              `type` = TaskTypes.Thread
            )
          )
          val oneOffSlot = SD.timeBlockTemplate.copy(
            start = startTime.plusHours(2),
            finish = startTime.plusHours(4),
            task = SD.taskTemplate.copy(
              taskId = None,
              `type` = TaskTypes.OneOff
            )
          )
          val weave = SD.timeBlockTemplate.copy(
            start = startTime.plusHours(4),
            finish = startTime.plusHours(6),
            task = SD.taskTemplate.copy(
              taskId = Some(weaveId),
              `type` = TaskTypes.Weave
            )
          )
          val currentTemplate = SD.timetableTemplate.copy(
            blocks = thread :: oneOffSlot :: weave :: Nil
          )
          val oneOff1 = SampleData.rest.oneOff.copy(
            uuid = UUID.randomUUID,
            estimate = 10800000L,
            status = Status.planned
          )
          val oneOff2 = SampleData.rest.oneOff.copy(
            uuid = UUID.randomUUID,
            estimate = 10800000L,
            status = Status.planned
          )

          val createCommand = SD.timetableCreation.copy(
            date = today,
            blocks = SD.scheduledTimeBlockCreation.copy(
              start = startTime,
              finish = startTime.plusHours(2),
              task = SD.scheduledTaskCreation.copy(
                taskId = threadId,
                `type` = TaskTypes.Thread
              )
            ) :: SD.scheduledTimeBlockCreation.copy(
              start = startTime.plusHours(2),
              finish = startTime.plusHours(4),
              task = SD.scheduledTaskCreation.copy(
                taskId = oneOff1.uuid,
                `type` = TaskTypes.OneOff
              )
            ) :: SD.scheduledTimeBlockCreation.copy(
              start = startTime.plusHours(4),
              finish = startTime.plusHours(6),
              task = SD.scheduledTaskCreation.copy(
                taskId = weaveId,
                `type` = TaskTypes.Weave
              )
            ) :: Nil
          )

          testTimetableGeneration(
            today = today,
            threadId = threadId,
            weaveId = weaveId
          )(
            currentTemplate = currentTemplate,
            oneOffs = oneOff1 :: oneOff2 :: Nil,
            createCommand = createCommand
          )()
        }

        it("should correctly handle a one-off having the exact same estimate as the slot") {
          val thread = SD.timeBlockTemplate.copy(
            start = startTime,
            finish = startTime.plusHours(2),
            task = SD.taskTemplate.copy(
              taskId = Some(threadId),
              `type` = TaskTypes.Thread
            )
          )
          val oneOffSlot = SD.timeBlockTemplate.copy(
            start = startTime.plusHours(2),
            finish = startTime.plusHours(4),
            task = SD.taskTemplate.copy(
              taskId = None,
              `type` = TaskTypes.OneOff
            )
          )
          val weave = SD.timeBlockTemplate.copy(
            start = startTime.plusHours(4),
            finish = startTime.plusHours(6),
            task = SD.taskTemplate.copy(
              taskId = Some(weaveId),
              `type` = TaskTypes.Weave
            )
          )
          val currentTemplate = SD.timetableTemplate.copy(
            blocks = thread :: oneOffSlot :: weave :: Nil
          )
          val oneOff1 = SampleData.rest.oneOff.copy(
            uuid = UUID.randomUUID,
            estimate = 7200000L,
            status = Status.planned
          )
          val oneOff2 = SampleData.rest.oneOff.copy(
            uuid = UUID.randomUUID,
            estimate = 10800000L,
            status = Status.planned
          )

          val createCommand = SD.timetableCreation.copy(
            date = today,
            blocks = SD.scheduledTimeBlockCreation.copy(
              start = startTime,
              finish = startTime.plusHours(2),
              task = SD.scheduledTaskCreation.copy(
                taskId = threadId,
                `type` = TaskTypes.Thread
              )
            ) :: SD.scheduledTimeBlockCreation.copy(
              start = startTime.plusHours(2),
              finish = startTime.plusHours(4),
              task = SD.scheduledTaskCreation.copy(
                taskId = oneOff1.uuid,
                `type` = TaskTypes.OneOff
              )
            ) :: SD.scheduledTimeBlockCreation.copy(
              start = startTime.plusHours(4),
              finish = startTime.plusHours(6),
              task = SD.scheduledTaskCreation.copy(
                taskId = weaveId,
                `type` = TaskTypes.Weave
              )
            ) :: Nil
          )

          testTimetableGeneration(
            today = today,
            threadId = threadId,
            weaveId = weaveId
          )(
            currentTemplate = currentTemplate,
            oneOffs = oneOff1 :: oneOff2 :: Nil,
            createCommand = createCommand
          )()
        }

        it("should correctly handle a one-off having a smaller estimate than the slot") {
          val thread = SD.timeBlockTemplate.copy(
            start = startTime,
            finish = startTime.plusHours(2),
            task = SD.taskTemplate.copy(
              taskId = Some(threadId),
              `type` = TaskTypes.Thread
            )
          )
          val oneOffSlot = SD.timeBlockTemplate.copy(
            start = startTime.plusHours(2),
            finish = startTime.plusHours(4),
            task = SD.taskTemplate.copy(
              taskId = None,
              `type` = TaskTypes.OneOff
            )
          )
          val weave = SD.timeBlockTemplate.copy(
            start = startTime.plusHours(4),
            finish = startTime.plusHours(6),
            task = SD.taskTemplate.copy(
              taskId = Some(weaveId),
              `type` = TaskTypes.Weave
            )
          )
          val currentTemplate = SD.timetableTemplate.copy(
            blocks = thread :: oneOffSlot :: weave :: Nil
          )
          val oneOff1 = SampleData.rest.oneOff.copy(
            uuid = UUID.randomUUID,
            estimate = 3600000L,
            status = Status.planned
          )
          val oneOff2 = SampleData.rest.oneOff.copy(
            uuid = UUID.randomUUID,
            estimate = 10800000L,
            status = Status.planned
          )

          val createCommand = SD.timetableCreation.copy(
            date = today,
            blocks = SD.scheduledTimeBlockCreation.copy(
              start = startTime,
              finish = startTime.plusHours(2),
              task = SD.scheduledTaskCreation.copy(
                taskId = threadId,
                `type` = TaskTypes.Thread
              )
            ) :: SD.scheduledTimeBlockCreation.copy(
              start = startTime.plusHours(2),
              finish = startTime.plusHours(3),
              task = SD.scheduledTaskCreation.copy(
                taskId = oneOff1.uuid,
                `type` = TaskTypes.OneOff
              )
            ) :: SD.scheduledTimeBlockCreation.copy(
              start = startTime.plusHours(3),
              finish = startTime.plusHours(4),
              task = SD.scheduledTaskCreation.copy(
                taskId = oneOff2.uuid,
                `type` = TaskTypes.OneOff
              )
            ) :: SD.scheduledTimeBlockCreation.copy(
              start = startTime.plusHours(4),
              finish = startTime.plusHours(6),
              task = SD.scheduledTaskCreation.copy(
                taskId = weaveId,
                `type` = TaskTypes.Weave
              )
            ) :: Nil
          )

          testTimetableGeneration(
            today = today,
            threadId = threadId,
            weaveId = weaveId
          )(
            currentTemplate = currentTemplate,
            oneOffs = oneOff1 :: oneOff2 :: Nil,
            createCommand = createCommand
          )()
        }

        it("should throw an exception if there is a one-off slot with no event to fill it with") {

        }
      }

      describe("with scheduled one-offs") {
        it("should correctly handle the absence of scheduled one-off slots and events") {

        }

        it("should correctly handle a scheduled event occurring within a block") {

        }

        it("should correctly handle a scheduled event occurring over the last half of a block") {

        }

        it("should correctly handle a scheduled event occurring over the first half of a block") {

        }
      }
    }
    describe("handling timetables") {
      it("should be able to update a timetable") {
        val server = newServer()
        when(server.repo.updateTimetable(eqTo(SD.timetableUpdate))).thenReturn(DBIOAction.successful(true))
        when(server.db.run(DBIOAction.successful(true))).thenReturn(Future.successful(true))
        whenReady(server.timetableService.updateCurrentTimetable(SD.timetableUpdate)) { result =>
          result shouldBe true
          verify(server.repo).updateTimetable(eqTo(SD.timetableUpdate))
          verifyNoMoreInteractions(server.repo)
        }
      }

      it("should be able to retrieve the current timetable") {
        val server = newServer()
        when(server.repo.currentTimetable).thenReturn(DBIOAction.successful(Some(SD.timetable)))
        when(server.db.run(DBIOAction.successful(Some(SD.timetable)))).thenReturn(Future.successful(Some(SD.timetable)))
        whenReady(server.timetableService.currentTimetable) { result =>
          result shouldBe Some(SD.timetable)
          verify(server.repo).currentTimetable
          verifyNoMoreInteractions(server.repo)
        }
      }
    }
  }

  def testTimetableGeneration(
    today: LocalDate = LocalDate.now,
    threadId: UUID = UUID.randomUUID,
    weaveId: UUID = UUID.randomUUID,
    portionId: UUID = UUID.randomUUID,
    currentPortion: PortionView = SampleData.rest.portion,
    portionSummary: String = "")(
    currentTemplate: TimetableTemplate = SD.timetableTemplate.copy(blocks = Nil),
    oneOffs: List[OneOffView] = Nil,
    scheduledOneOffs: List[ScheduledOneOffView] = Nil,
    createCommand: CreateTimetable = SD.timetableCreation.copy(date = today, blocks = Nil),
    timetableInRepo: Timetable = SD.timetable.copy(date = today, blocks = Nil),
    timetableView: TimetableView = SampleData.rest.timetable.copy(date = today, blocks = Nil))(
    verifications: Server => Unit = _ => ()
  ): Unit = {
    val server = newServer()
    when(server.repo.currentTemplate).thenReturn(DBIOAction.successful(Some(currentTemplate)))
    when(server.db.run(DBIOAction.successful(Some(currentTemplate)))).thenReturn(Future.successful(Some(currentTemplate)))
    when(server.timer.today).thenReturn(today)
    when(server.planningService.currentPortion).thenReturn(Future.successful(Some(currentPortion)))
    when(server.repo.createTimetable(createCommand)).thenReturn(DBIOAction.successful(timetableInRepo))
    when(server.db.run(eqTo(DBIOAction.successful(timetableInRepo)))).thenReturn(Future.successful(timetableInRepo))
    when(server.db.run(DBIOAction.successful(timetableInRepo))).thenReturn(Future.successful(timetableInRepo))
    when(server.planningService.thread(threadId)).thenReturn(Future.successful(Some(SampleData.rest.thread)))
    when(server.planningService.weave(weaveId)).thenReturn(Future.successful(Some(SampleData.rest.weave)))
    when(server.planningService.portion(currentPortion.uuid)).thenReturn(Future.successful(Some(SampleData.rest.portion.copy(summary = portionSummary))))
    when(server.planningService.portion(portionId)).thenReturn(Future.successful(Some(SampleData.rest.portion)))
    when(server.planningService.oneOffs).thenReturn(Future.successful(oneOffs))
    when(server.planningService.scheduledOneOffs(Some(today))).thenReturn(Future.successful(scheduledOneOffs))
    whenReady(server.timetableService.generateTimetable) { result =>
      result shouldBe timetableView
      verifications
    }
  }
}
