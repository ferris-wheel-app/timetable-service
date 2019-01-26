package com.ferris.timetable.repo

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalTime}
import java.util.UUID

import com.ferris.timetable.model.Model.TaskTypes
import org.scalatest.{AsyncFunSpec, BeforeAndAfterEach, Matchers, OneInstancePerTest}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.OptionValues._
import com.ferris.timetable.sample.SampleData.{domain => SD}
import com.ferris.timetable.service.exceptions.Exceptions._
import com.ferris.utils.MockTimerComponent
import org.mockito.Mockito._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class TimetableRepositoryTest extends AsyncFunSpec
  with OneInstancePerTest
  with Matchers
  with ScalaFutures
  with BeforeAndAfterEach
  with H2TimetableRepositoryComponent
  with MockTimerComponent {

  implicit val dbTimeout: FiniteDuration = 40.seconds
  implicit val defaultPatience = PatienceConfig(timeout = 5.seconds, interval = 500.millis)

  override val executionContext: ExecutionContext = repoEc

  override def beforeEach(): Unit = {
    RepositoryUtils.createOrResetTables(db, dbTimeout)(repoEc)
    super.beforeEach
  }

  describe("routine") {
    describe("creating") {
      it("should create a routine") {
        val created = db.run(repo.createRoutine(SD.routineCreation)).futureValue
        created.name shouldBe SD.routineCreation.name
        created.monday shouldBe SD.routine.monday
        created.tuesday shouldBe SD.routine.tuesday
        created.wednesday shouldBe SD.routine.wednesday
        created.thursday shouldBe SD.routine.thursday
        created.friday shouldBe SD.routine.friday
        created.saturday shouldBe SD.routine.saturday
        created.sunday shouldBe SD.routine.sunday
        created.isCurrent shouldBe false
      }
    }

    describe("updating") {
      it("should update a routine") {
        val original = db.run(repo.createRoutine(SD.routineCreation)).futureValue
        val taskId = UUID.randomUUID
        val updateCommmand = SD.routineUpdate.copy(
          name = Some("Summer"),
          monday = Some(SD.timetableTemplateCreation.copy(
            blocks = SD.timeBlockTemplateCreation.copy(
              task = SD.taskTemplateCreation.copy(
                taskId = Some(taskId),
                `type` = TaskTypes.Thread
              )
            ) :: SD.timeBlockTemplateCreation.copy(
              task = SD.taskTemplateCreation.copy(
                taskId = None,
                `type` = TaskTypes.Hobby
              )
            ) :: Nil
          ))
        )
        val expected = SD.routine.copy(
          name = "Summer",
          monday = SD.timetableTemplate.copy(
            blocks = SD.timeBlockTemplate.copy(
              task = SD.taskTemplate.copy(
                taskId = Some(taskId),
                `type` = TaskTypes.Thread
              )
            ) :: SD.timeBlockTemplate.copy(
              task = SD.taskTemplate.copy(
                taskId = None,
                `type` = TaskTypes.Hobby
              )
            ) :: Nil
          )
        )
        val updateStatus = db.run(repo.updateRoutine(original.uuid, updateCommmand)).futureValue
        val updated = db.run(repo.getRoutine(original.uuid)).futureValue.value

        updateStatus shouldBe true
        updated should not be original
        updated.uuid shouldBe original.uuid
        updated.monday shouldBe expected.monday
        updated.tuesday shouldBe SD.routine.tuesday
        updated.wednesday shouldBe SD.routine.wednesday
        updated.thursday shouldBe SD.routine.thursday
        updated.friday shouldBe SD.routine.friday
        updated.saturday shouldBe SD.routine.saturday
        updated.sunday shouldBe SD.routine.sunday
        updated.isCurrent shouldBe false
      }

      it("should throw an exception if the routine is not found") {
        whenReady(db.run(repo.updateRoutine(UUID.randomUUID, SD.routineUpdate)).failed) { exception =>
          exception shouldBe RoutineNotFoundException()
        }
      }
    }

    describe("starting") {
      it("should start a routine") {
        val firstRoutine = db.run(repo.createRoutine(SD.routineCreation)).futureValue
        val secondRoutine = db.run(repo.createRoutine(SD.routineCreation)).futureValue
        val firstUpdate = db.run(repo.startRoutine(firstRoutine.uuid)).futureValue
        val firstRoutineStarted = db.run(repo.getRoutine(firstRoutine.uuid)).futureValue.value
        val secondRoutineNotStarted = db.run(repo.getRoutine(secondRoutine.uuid)).futureValue.value

        firstRoutine.isCurrent shouldBe false
        secondRoutine.isCurrent shouldBe false
        firstUpdate shouldBe true
        firstRoutineStarted.isCurrent shouldBe true
        secondRoutineNotStarted.isCurrent shouldBe false

        val secondUpdate = db.run(repo.startRoutine(secondRoutine.uuid)).futureValue
        val firstRoutineStopped = db.run(repo.getRoutine(firstRoutine.uuid)).futureValue.value
        val secondRoutineStarted = db.run(repo.getRoutine(secondRoutine.uuid)).futureValue.value

        secondUpdate shouldBe true
        firstRoutineStopped.isCurrent shouldBe false
        secondRoutineStarted.isCurrent shouldBe true
      }

      it("should throw an exception if the routine is not found") {
        whenReady(db.run(repo.startRoutine(UUID.randomUUID)).failed) { exception =>
          exception shouldBe RoutineNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a routine") {
        val created = db.run(repo.createRoutine(SD.routineCreation)).futureValue
        val retrieved = db.run(repo.getRoutine(created.uuid)).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should return none if a routine is not found") {
        val retrieved = db.run(repo.getRoutine(UUID.randomUUID)).futureValue
        retrieved shouldBe empty
      }

      it("should retrieve a list of routines") {
        val created1 = db.run(repo.createRoutine(SD.routineCreation)).futureValue
        val created2 = db.run(repo.createRoutine(SD.routineCreation.copy(name = "Spring"))).futureValue
        val retrieved = db.run(repo.getRoutines).futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }
    }

    describe("deleting") {
      it("should delete a routine") {
        val created = db.run(repo.createRoutine(SD.routineCreation)).futureValue
        val deletion = db.run(repo.deleteRoutine(created.uuid)).futureValue
        val retrieved = db.run(repo.getRoutine(created.uuid)).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
  }

  describe("a current template") {
    it("should be possible to retrieve if it exists") {
      val taskId = UUID.randomUUID
      val routineCreation = SD.routineCreation.copy(
        name = "Summer",
        friday = SD.timetableTemplateCreation.copy(
          blocks = SD.timeBlockTemplateCreation.copy(
            task = SD.taskTemplateCreation.copy(
              taskId = Some(taskId),
              `type` = TaskTypes.Thread
            )
          ) :: SD.timeBlockTemplateCreation.copy(
            task = SD.taskTemplateCreation.copy(
              taskId = None,
              `type` = TaskTypes.Hobby
            )
          ) :: Nil
        )
      )
      val expectedTemplate = SD.timetableTemplate.copy(
        blocks = SD.timeBlockTemplate.copy(
          task = SD.taskTemplate.copy(
            taskId = Some(taskId),
            `type` = TaskTypes.Thread
          )
        ) :: SD.timeBlockTemplate.copy(
          task = SD.taskTemplate.copy(
            taskId = None,
            `type` = TaskTypes.Hobby
          )
        ) :: Nil
      )
      when(timer.today).thenReturn(LocalDate.of(2018, 9, 21)) // Friday

      db.run(repo.createRoutine(SD.routineCreation)).futureValue
      val chosenRoutine = db.run(repo.createRoutine(routineCreation)).futureValue
      db.run(repo.startRoutine(chosenRoutine.uuid)).futureValue

      val chosenTemplate = db.run(repo.currentTemplate).futureValue

      chosenTemplate should not be empty
      chosenTemplate.value shouldBe expectedTemplate
    }

    it("should return none if it does not exist") {
      db.run(repo.createRoutine(SD.routineCreation)).futureValue
      db.run(repo.createRoutine(SD.routineCreation)).futureValue

      val startedRoutine = db.run(repo.currentTemplate).futureValue

      startedRoutine shouldBe empty
    }
  }

  describe("timetable") {
    describe("creating") {
      it("should create a timetable") {
        val blocks = SD.concreteBlock.copy(
          task = SD.scheduledTask.copy(
            taskId = SD.scheduledTaskCreation.taskId,
            `type` = SD.scheduledTaskCreation.`type`
          )
        ) :: Nil
        val created = db.run(repo.createTimetable(SD.timetableCreation)).futureValue
        created shouldBe SD.timetable.copy(blocks = blocks)
      }
    }

    describe("retrieving") {
      it("should get the current timetable") {
        val taskId = UUID.randomUUID
        val startTime = LocalTime.now.truncatedTo(ChronoUnit.MINUTES)
        val finishTime = startTime.plusHours(2L)
        val blocksCreation = SD.scheduledTimeBlockCreation.copy(start = startTime, finish = finishTime, task = SD.scheduledTaskCreation.copy(taskId = taskId)) :: Nil
        val created = db.run(repo.createTimetable(SD.timetableCreation.copy(blocks = blocksCreation))).futureValue
        val currentTimetable = db.run(repo.currentTimetable).futureValue

        currentTimetable.value shouldBe created
      }
    }

    describe("slot retrieving") {
      val firstStart = LocalTime.now.truncatedTo(ChronoUnit.MINUTES)
      val firstFinish = firstStart.plusHours(2L)
      val secondStart = firstFinish
      val secondFinish = secondStart.plusHours(2L)
      val thirdStart = secondFinish
      val thirdFinish = thirdStart.plusHours(2L)
      val firstBlockCreation = SD.scheduledTimeBlockCreation.copy(start = firstStart, finish = firstFinish, task = SD.scheduledTaskCreation)
      val secondBlockCreation = SD.scheduledTimeBlockCreation.copy(start = secondStart, finish = secondFinish, task = SD.scheduledTaskCreation)
      val thirdBlockCreation = SD.scheduledTimeBlockCreation.copy(start = thirdStart, finish = thirdFinish, task = SD.scheduledTaskCreation)
      val secondBlock = SD.concreteBlock.copy(start = secondStart, finish = secondFinish, task = SD.scheduledTask.copy(taskId = secondBlockCreation.task.taskId))
      val blocksCreation = firstBlockCreation :: secondBlockCreation :: thirdBlockCreation :: Nil

      it("should get a block based on the start and finish times") {
        db.run(repo.createTimetable(SD.timetableCreation.copy(blocks = blocksCreation))).futureValue
        val slot = db.run(repo.getSlot(secondStart, secondFinish)).futureValue

        slot.value shouldBe secondBlock
      }

      it("should return an empty Option, if it is not found") {
        db.run(repo.createTimetable(SD.timetableCreation.copy(blocks = blocksCreation))).futureValue
        val slot = db.run(repo.getSlot(secondStart, thirdFinish)).futureValue

        slot shouldBe empty
      }
    }

    describe("updating") {
      it("should update a timetable") {
        val taskId = UUID.randomUUID
        val startTime = LocalTime.now.truncatedTo(ChronoUnit.MINUTES)
        val finishTime = startTime.plusHours(2L)
        val laterStartTime = finishTime
        val laterFinishTime = finishTime.plusHours(2L)
        val firstTask = SD.scheduledTask.copy(taskId = taskId, isDone = false)
        val secondTask = SD.scheduledTask.copy(taskId = taskId, isDone = false)
        val firstBlock = SD.concreteBlock.copy(start = startTime, finish = finishTime, task = firstTask)
        val secondBlock = SD.concreteBlock.copy(start = laterStartTime, finish = laterFinishTime, task = secondTask)
        val blocksCreation = SD.scheduledTimeBlockCreation.copy(start = startTime, finish = finishTime, task = SD.scheduledTaskCreation.copy(taskId = taskId)) ::
          SD.scheduledTimeBlockCreation.copy(start = laterStartTime, finish = laterFinishTime, task = SD.scheduledTaskCreation.copy(taskId = taskId)) :: Nil
        val blocksUpdate = SD.scheduledTimeBlockUpdate.copy(start = startTime, finish = finishTime, done = true) :: Nil
        val created = db.run(repo.createTimetable(SD.timetableCreation.copy(blocks = blocksCreation))).futureValue
        val updateStatus = db.run(repo.updateTimetable(SD.timetableUpdate.copy(blocks = blocksUpdate))).futureValue
        val updated = db.run(repo.currentTimetable).futureValue
        val expected = created.copy(blocks = firstBlock.copy(task = firstTask.copy(isDone = true)) :: secondBlock :: Nil)

        updateStatus shouldBe true
        updated should not be empty
        updated.value should not be created
        updated.value shouldBe expected
      }

      it("should return false if the specified time-slot is not found") {
        val taskId = UUID.randomUUID
        val startTime = LocalTime.now.truncatedTo(ChronoUnit.MINUTES)
        val finishTime = startTime.plusHours(2L)
        val laterStartTime = finishTime
        val laterFinishTime = finishTime.plusHours(2L)
        val blocksCreation = SD.scheduledTimeBlockCreation.copy(start = startTime, finish = finishTime, task = SD.scheduledTaskCreation.copy(taskId = taskId)) :: Nil
        val blocksUpdate = SD.scheduledTimeBlockUpdate.copy(start = laterStartTime, finish = laterFinishTime, done = true) :: Nil
        val created = db.run(repo.createTimetable(SD.timetableCreation.copy(blocks = blocksCreation))).futureValue
        val updateStatus = db.run(repo.updateTimetable(SD.timetableUpdate.copy(blocks = blocksUpdate))).futureValue
        val updated = db.run(repo.currentTimetable).futureValue

        updateStatus shouldBe false
        updated should not be empty
        updated.value shouldBe created
      }
    }
  }
}
