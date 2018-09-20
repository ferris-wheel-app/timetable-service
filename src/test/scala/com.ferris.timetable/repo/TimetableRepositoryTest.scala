package com.ferris.timetable.repo

import java.util.UUID

import com.ferris.timetable.model.Model.TaskTypes
import org.scalatest.{AsyncFunSpec, BeforeAndAfterEach, Matchers, OneInstancePerTest}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.OptionValues._
import com.ferris.timetable.sample.SampleData.{domain => SD}
import com.ferris.timetable.service.exceptions.Exceptions._
import com.ferris.utils.MockTimerComponent

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

  describe("message") {
    describe("creating") {
      it("should create a message") {
        val created = db.run(repo.createMessage(SD.messageCreation)).futureValue
        created.sender shouldBe SD.messageCreation.sender
        created.content shouldBe SD.messageCreation.content
      }
    }

    describe("updating") {
      it("should update a message") {
        val original = db.run(repo.createMessage(SD.messageCreation)).futureValue
        val updated = db.run(repo.updateMessage(original.uuid, SD.messageUpdate)).futureValue
        updated should not be original
        updated.uuid shouldBe original.uuid
        updated.sender shouldBe SD.messageUpdate.sender.value
        updated.content shouldBe SD.messageUpdate.content.value
      }

      it("should throw an exception if a message is not found") {
        whenReady(db.run(repo.updateMessage(UUID.randomUUID, SD.messageUpdate)).failed) { exception =>
          exception shouldBe MessageNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a message") {
        val created = db.run(repo.createMessage(SD.messageCreation)).futureValue
        val retrieved = db.run(repo.getMessage(created.uuid)).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should return none if a message is not found") {
        val retrieved = db.run(repo.getMessage(UUID.randomUUID)).futureValue
        retrieved shouldBe empty
      }

      it("should retrieve a list of messages") {
        val created1 = db.run(repo.createMessage(SD.messageCreation)).futureValue
        val created2 = db.run(repo.createMessage(SD.messageCreation.copy(sender = "HAL", content = "Never!"))).futureValue
        val retrieved = db.run(repo.getMessages).futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }
    }

    describe("deleting") {
      it("should delete a message") {
        val created = db.run(repo.createMessage(SD.messageCreation)).futureValue
        val deletion = db.run(repo.deleteMessage(created.uuid)).futureValue
        val retrieved = db.run(repo.getMessage(created.uuid)).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
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
        val routine = db.run(repo.createRoutine(SD.routineCreation)).futureValue
        val updateResult = db.run(repo.startRoutine(routine.uuid)).futureValue
        val startedRoutine = db.run(repo.getRoutine(routine.uuid)).futureValue.value

        routine.isCurrent shouldBe false
        updateResult shouldBe true
        startedRoutine.isCurrent shouldBe true
      }

      it("should throw an exception if the routine is not found") {
        whenReady(db.run(repo.startRoutine(UUID.randomUUID)).failed) { exception =>
          exception shouldBe RoutineNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a routine") {
        ???
      }

      it("should return none if a routine is not found") {
        ???
      }

      it("should retrieve a list of routines") {
        ???
      }
    }

    describe("deleting") {
      it("should delete a routine") {
        ???
      }
    }
  }

  describe("a current template") {
    it("should be possible to retrieve if it exists") {
      ???
    }

    it("should return none if it does not exist") {
      ???
    }
  }

  describe("timetable") {
    describe("creating") {
      it("should create a timetable") {
        ???
      }
    }

    describe("updating") {
      it("should update a timetable") {
        ???
      }
    }

    describe("retrieving") {
      it("should get the current timetable") {
        ???
      }
    }
  }
}











































