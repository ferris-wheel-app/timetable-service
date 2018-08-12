package com.ferris.timetable.repo

import java.util.UUID

import org.scalatest.{AsyncFunSpec, BeforeAndAfterEach, Matchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.OptionValues._
import com.ferris.timetable.sample.SampleData.{domain => SD}
import com.ferris.timetable.service.exceptions.Exceptions._
import com.ferris.utils.MockTimerComponent

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class TimetableRepositoryTest extends AsyncFunSpec
  with Matchers
  with ScalaFutures
  with BeforeAndAfterEach
  with H2TimetableRepositoryComponent
  with MockTimerComponent {

  implicit val dbTimeout: FiniteDuration = 20.seconds

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
}
