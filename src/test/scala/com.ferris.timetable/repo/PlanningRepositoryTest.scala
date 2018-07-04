package com.ferris.timetable.repo

import java.time.LocalDateTime
import java.util.UUID

import com.ferris.planning.command.Commands.UpdateList
import com.ferris.planning.config.PlanningServiceConfig
import com.ferris.planning.model.Model._
import com.ferris.planning.model.Model.Statuses._
import org.scalatest.{AsyncFunSpec, BeforeAndAfterEach, Matchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.OptionValues._
import com.ferris.planning.sample.SampleData.{domain => SD}
import com.ferris.planning.scheduler.MockLifeSchedulerComponent
import com.ferris.planning.service.exceptions.Exceptions._
import com.ferris.planning.utils.MockTimerComponent
import com.ferris.planning.utils.PlanningImplicits._
import org.mockito.Matchers.{eq => eqTo}
import org.mockito.Mockito.when

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class PlanningRepositoryTest extends AsyncFunSpec
  with Matchers
  with ScalaFutures
  with BeforeAndAfterEach
  with H2PlanningRepositoryComponent
  with MockTimerComponent
  with MockLifeSchedulerComponent {

  implicit val dbTimeout: FiniteDuration = 20.seconds

  override val config: PlanningServiceConfig = PlanningServiceConfig(acceptableProgress = 80)

  override val executionContext: ExecutionContext = repoEc

  override def beforeEach(): Unit = {
    RepositoryUtils.createOrResetTables(db, dbTimeout)(repoEc)
    super.beforeEach
  }

  describe("message") {
    describe("creating") {
      it("should create a message") {
        val created = repo.createMessage(SD.messageCreation).futureValue
        created.sender shouldBe SD.messageCreation.sender
        created.content shouldBe SD.messageCreation.content
      }
    }

    describe("updating") {
      it("should update a message") {
        val original = repo.createMessage(SD.messageCreation).futureValue
        val updated = repo.updateMessage(original.uuid, SD.messageUpdate).futureValue
        updated should not be original
        updated.uuid shouldBe original.uuid
        updated.sender shouldBe SD.messageUpdate.sender.value
        updated.content shouldBe SD.messageUpdate.content.value
      }

      it("should throw an exception if a message is not found") {
        whenReady(repo.updateMessage(UUID.randomUUID, SD.messageUpdate).failed) { exception =>
          exception shouldBe MessageNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a message") {
        val created = repo.createMessage(SD.messageCreation).futureValue
        val retrieved = repo.getMessage(created.uuid).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should return none if a message is not found") {
        val retrieved = repo.getMessage(UUID.randomUUID).futureValue
        retrieved shouldBe empty
      }

      it("should retrieve a list of messages") {
        val created1 = repo.createMessage(SD.messageCreation).futureValue
        val created2 = repo.createMessage(SD.messageCreation.copy(sender = "HAL", content = "Never!")).futureValue
        val retrieved = repo.getMessages.futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }
    }

    describe("deleting") {
      it("should delete a message") {
        val created = repo.createMessage(SD.messageCreation).futureValue
        val deletion = repo.deleteMessage(created.uuid).futureValue
        val retrieved = repo.getMessage(created.uuid).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
  }

  describe("backlog item") {
    describe("creating") {
      it("should create a backlog item") {
        val created = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        created.summary shouldBe SD.backlogItemCreation.summary
        created.description shouldBe SD.backlogItemCreation.description
        created.`type` shouldBe SD.backlogItemCreation.`type`
      }
    }

    describe("updating") {
      it("should update a backlog item") {
        val original = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val updated = repo.updateBacklogItem(original.uuid, SD.backlogItemUpdate).futureValue
        updated should not be original
        updated.uuid shouldBe original.uuid
        updated.summary shouldBe SD.backlogItemUpdate.summary.value
        updated.description shouldBe SD.backlogItemUpdate.description.value
        updated.`type` shouldBe SD.backlogItemUpdate.`type`.value
      }

      it("should throw an exception if a message is not found") {
        whenReady(repo.updateBacklogItem(UUID.randomUUID, SD.backlogItemUpdate).failed) { exception =>
          exception shouldBe BacklogItemNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a backlog-item") {
        val created = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val retrieved = repo.getBacklogItem(created.uuid).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should return none if a backlog-item is not found") {
        val retrieved = repo.getBacklogItem(UUID.randomUUID).futureValue
        retrieved shouldBe empty
      }

      it("should retrieve a list of backlog-items") {
        val created1 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val created2 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val retrieved = repo.getBacklogItems.futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }
    }

    describe("deleting") {
      it("should delete a backlog-item") {
        val created = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val deletion = repo.deleteBacklogItem(created.uuid).futureValue
        val retrieved = repo.getBacklogItem(created.uuid).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
  }

  describe("epoch") {
    describe("creating") {
      it("should create an epoch") {
        val created = repo.createEpoch(SD.epochCreation).futureValue
        created.name shouldBe SD.epochCreation.name
        created.totem shouldBe SD.epochCreation.totem
        created.question shouldBe SD.epochCreation.question
      }
    }

    describe("updating") {
      it("should update an epoch") {
        val original = repo.createEpoch(SD.epochCreation).futureValue
        val updated = repo.updateEpoch(original.uuid, SD.epochUpdate).futureValue
        updated.uuid shouldBe original.uuid
        updated.name shouldBe SD.epochUpdate.name.value
        updated.totem shouldBe SD.epochUpdate.totem.value
        updated.question shouldBe SD.epochUpdate.question.value
      }

      it("should throw an exception if an epoch is not found") {
        whenReady(repo.updateEpoch(UUID.randomUUID, SD.epochUpdate).failed) { exception =>
          exception shouldBe EpochNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve an epoch") {
        val created = repo.createEpoch(SD.epochCreation).futureValue
        val retrieved = repo.getEpoch(created.uuid).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should return none if an epoch is not found") {
        val retrieved = repo.getEpoch(UUID.randomUUID).futureValue
        retrieved shouldBe empty
      }

      it("should retrieve a list of epochs") {
        val created1 = repo.createEpoch(SD.epochCreation).futureValue
        val created2 = repo.createEpoch(SD.epochCreation).futureValue
        val retrieved = repo.getEpochs.futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }
    }

    describe("deleting") {
      it("should delete a epoch") {
        val created = repo.createEpoch(SD.epochCreation).futureValue
        val deletion = repo.deleteEpoch(created.uuid).futureValue
        val retrieved = repo.getEpoch(created.uuid).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
  }

  describe("year") {
    describe("creating") {
      it("should create a year") {
        val created = repo.createYear(SD.yearCreation).futureValue
        created.epochId shouldBe SD.yearCreation.epochId
        created.startDate shouldBe SD.yearCreation.startDate
        created.finishDate shouldBe SD.yearCreation.startDate.plusYears(1)
      }
    }

    describe("updating") {
      it("should update a year") {
        val original = repo.createYear(SD.yearCreation).futureValue
        val updated = repo.updateYear(original.uuid, SD.yearUpdate).futureValue
        updated.uuid shouldBe original.uuid
        updated.epochId shouldBe SD.yearUpdate.epochId.value
        updated.startDate shouldBe SD.yearUpdate.startDate.value
        updated.finishDate shouldBe SD.yearUpdate.startDate.value.plusYears(1)
      }

      it("should throw an exception if a year is not found") {
        whenReady(repo.updateYear(UUID.randomUUID, SD.yearUpdate).failed) { exception =>
          exception shouldBe YearNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a year") {
        val created = repo.createYear(SD.yearCreation).futureValue
        val retrieved = repo.getYear(created.uuid).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should return none if a year is not found") {
        val retrieved = repo.getYear(UUID.randomUUID).futureValue
        retrieved shouldBe empty
      }

      it("should retrieve a list of years") {
        val created1 = repo.createYear(SD.yearCreation).futureValue
        val created2 = repo.createYear(SD.yearCreation).futureValue
        val retrieved = repo.getYears.futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }
    }

    describe("deleting") {
      it("should delete a year") {
        val created = repo.createYear(SD.yearCreation).futureValue
        val deletion = repo.deleteYear(created.uuid).futureValue
        val retrieved = repo.getYear(created.uuid).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
  }

  describe("theme") {
    describe("creating") {
      it("should create a theme") {
        val created = repo.createTheme(SD.themeCreation).futureValue
        created.yearId shouldBe SD.themeCreation.yearId
        created.name shouldBe SD.themeCreation.name
      }
    }

    describe("updating") {
      it("should update a theme") {
        val original = repo.createTheme(SD.themeCreation).futureValue
        val updated = repo.updateTheme(original.uuid, SD.themeUpdate).futureValue
        updated should not be original
        updated.uuid shouldBe original.uuid
        updated.yearId shouldBe SD.themeUpdate.yearId.value
        updated.name shouldBe SD.themeUpdate.name.value
      }

      it("should throw an exception if a theme is not found") {
        whenReady(repo.updateTheme(UUID.randomUUID, SD.themeUpdate).failed) { exception =>
          exception shouldBe ThemeNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a theme") {
        val created = repo.createTheme(SD.themeCreation).futureValue
        val retrieved = repo.getTheme(created.uuid).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should return none if a theme is not found") {
        val retrieved = repo.getTheme(UUID.randomUUID).futureValue
        retrieved shouldBe empty
      }

      it("should retrieve a list of themes") {
        val created1 = repo.createTheme(SD.themeCreation).futureValue
        val created2 = repo.createTheme(SD.themeCreation).futureValue
        val retrieved = repo.getThemes.futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }
    }

    describe("deleting") {
      it("should delete a theme") {
        val created = repo.createTheme(SD.themeCreation).futureValue
        val deletion = repo.deleteTheme(created.uuid).futureValue
        val retrieved = repo.getTheme(created.uuid).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
  }

  describe("goal") {
    describe("creating") {
      it("should create a goal") {
        val backlogItem1 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val backlogItem2 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val backlogItems =  backlogItem1.uuid :: backlogItem2.uuid :: Nil
        val created = repo.createGoal(SD.goalCreation.copy(backlogItems = backlogItems)).futureValue
        created.themeId shouldBe SD.goalCreation.themeId
        created.backlogItems should contain theSameElementsAs backlogItems
        created.summary shouldBe SD.goalCreation.summary
        created.description shouldBe SD.goalCreation.description
        created.level shouldBe SD.goalCreation.level
        created.priority shouldBe SD.goalCreation.priority
        created.graduation shouldBe SD.goalCreation.graduation
        created.status shouldBe SD.goalCreation.status
      }
    }

    describe("updating") {
      it("should update a goal") {
        val backlogItem1 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val backlogItem2 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val backlogItem3 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val backlogItem4 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val originalBacklogItems =  backlogItem1.uuid :: backlogItem2.uuid :: Nil
        val newBacklogItems =  backlogItem3.uuid :: backlogItem4.uuid :: Nil
        val original = repo.createGoal(SD.goalCreation.copy(backlogItems = originalBacklogItems)).futureValue
        val updated = repo.updateGoal(original.uuid, SD.goalUpdate.copy(backlogItems = Some(newBacklogItems))).futureValue
        updated.uuid shouldBe original.uuid
        updated.themeId shouldBe SD.goalUpdate.themeId.value
        updated.backlogItems should contain theSameElementsAs newBacklogItems
        updated.summary shouldBe SD.goalUpdate.summary.value
        updated.description shouldBe SD.goalUpdate.description.value
        updated.level shouldBe SD.goalUpdate.level.value
        updated.priority shouldBe SD.goalUpdate.priority.value
        updated.graduation shouldBe SD.goalUpdate.graduation.value
        updated.status shouldBe SD.goalUpdate.status.value
      }

      it("should throw an exception if a goal is not found") {
        whenReady(repo.updateGoal(UUID.randomUUID, SD.goalUpdate).failed) { exception =>
          exception shouldBe GoalNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a goal with backlog items") {
        val backlogItem1 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val backlogItem2 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val backlogItems =  backlogItem1.uuid :: backlogItem2.uuid :: Nil
        val created = repo.createGoal(SD.goalCreation.copy(backlogItems = backlogItems)).futureValue
        val retrieved = repo.getGoal(created.uuid).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should retrieve a goal without backlog items") {
        val created = repo.createGoal(SD.goalCreation.copy(backlogItems = Nil)).futureValue
        val retrieved = repo.getGoal(created.uuid).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should retrieve a list of goals with backlog items") {
        val backlogItem1 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val backlogItem2 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val backlogItems =  backlogItem1.uuid :: backlogItem2.uuid :: Nil
        val created1 = repo.createGoal(SD.goalCreation.copy(backlogItems = backlogItems)).futureValue
        val created2 = repo.createGoal(SD.goalCreation.copy(backlogItems = backlogItems)).futureValue
        val retrieved = repo.getGoals.futureValue
        retrieved should not be empty
        retrieved should contain theSameElementsAs Seq(created1, created2)
      }

      it("should retrieve a list of goals without backlog items") {
        val created1 = repo.createGoal(SD.goalCreation.copy(backlogItems = Nil)).futureValue
        val created2 = repo.createGoal(SD.goalCreation.copy(backlogItems = Nil)).futureValue
        val retrieved = repo.getGoals.futureValue
        retrieved should not be empty
        retrieved should contain theSameElementsAs Seq(created1, created2)
      }

      it("should return none if a goal is not found") {
        val retrieved = repo.getGoal(UUID.randomUUID).futureValue
        retrieved shouldBe empty
      }
    }

    describe("deleting") {
      it("should delete a goal") {
        val backlogItem1 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val backlogItem2 = repo.createBacklogItem(SD.backlogItemCreation).futureValue
        val backlogItems =  backlogItem1.uuid :: backlogItem2.uuid :: Nil
        val created = repo.createGoal(SD.goalCreation.copy(backlogItems = backlogItems)).futureValue
        val deletion = repo.deleteGoal(created.uuid).futureValue
        val retrieved = repo.getGoal(created.uuid).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
  }

  describe("thread") {
    describe("creating") {
      it("should create a thread") {
        val created = repo.createThread(SD.threadCreation).futureValue
        created.goalId shouldBe SD.threadCreation.goalId
        created.summary shouldBe SD.threadCreation.summary
        created.description shouldBe SD.threadCreation.description
        created.status shouldBe SD.threadCreation.status
      }
    }

    describe("updating") {
      it("should update a thread") {
        val original = repo.createThread(SD.threadCreation).futureValue
        val updated = repo.updateThread(original.uuid, SD.threadUpdate).futureValue
        updated should not be original
        updated.uuid shouldBe original.uuid
        updated.goalId.value shouldBe SD.threadUpdate.goalId.value
        updated.summary shouldBe SD.threadUpdate.summary.value
        updated.description shouldBe SD.threadUpdate.description.value
        updated.status shouldBe SD.threadUpdate.status.value
      }

      it("should throw an exception if a thread is not found") {
        whenReady(repo.updateThread(UUID.randomUUID, SD.threadUpdate).failed) { exception =>
          exception shouldBe ThreadNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a thread") {
        val created = repo.createThread(SD.threadCreation).futureValue
        val retrieved = repo.getThread(created.uuid).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should return none if a thread is not found") {
        val retrieved = repo.getThread(UUID.randomUUID).futureValue
        retrieved shouldBe empty
      }

      it("should retrieve a list of threads") {
        val created1 = repo.createThread(SD.threadCreation).futureValue
        val created2 = repo.createThread(SD.threadCreation).futureValue
        val retrieved = repo.getThreads.futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }

      it("should retrieve a list of threads based on the goal they belong to") {
        val goalId = UUID.randomUUID
        val otherGoalId = UUID.randomUUID
        val created1 = repo.createThread(SD.threadCreation.copy(goalId = Some(goalId))).futureValue
        val created2 = repo.createThread(SD.threadCreation.copy(goalId = Some(goalId))).futureValue
        repo.createThread(SD.threadCreation.copy(goalId = Some(otherGoalId))).futureValue
        val retrieved = repo.getThreads(goalId).futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }
    }

    describe("deleting") {
      it("should delete a thread") {
        val created = repo.createThread(SD.threadCreation).futureValue
        val deletion = repo.deleteThread(created.uuid).futureValue
        val retrieved = repo.getThread(created.uuid).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
  }

  describe("weave") {
    describe("creating") {
      it("should create a weave") {
        val created = repo.createWeave(SD.weaveCreation).futureValue
        created.goalId shouldBe SD.weaveCreation.goalId
        created.summary shouldBe SD.weaveCreation.summary
        created.description shouldBe SD.weaveCreation.description
        created.`type` shouldBe SD.weaveCreation.`type`
        created.status shouldBe SD.weaveCreation.status
      }
    }

    describe("updating") {
      it("should update a weave") {
        val original = repo.createWeave(SD.weaveCreation).futureValue
        val updated = repo.updateWeave(original.uuid, SD.weaveUpdate).futureValue
        updated should not be original
        updated.uuid shouldBe original.uuid
        updated.goalId.value shouldBe SD.weaveUpdate.goalId.value
        updated.summary shouldBe SD.weaveUpdate.summary.value
        updated.description shouldBe SD.weaveUpdate.description.value
        updated.`type` shouldBe SD.weaveUpdate.`type`.value
        updated.status shouldBe SD.weaveUpdate.status.value
      }

      it("should throw an exception if a weave is not found") {
        whenReady(repo.updateWeave(UUID.randomUUID, SD.weaveUpdate).failed) { exception =>
          exception shouldBe WeaveNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a weave") {
        val created = repo.createWeave(SD.weaveCreation).futureValue
        val retrieved = repo.getWeave(created.uuid).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should return none if a weave is not found") {
        val retrieved = repo.getWeave(UUID.randomUUID).futureValue
        retrieved shouldBe empty
      }

      it("should retrieve a list of weaves") {
        val created1 = repo.createWeave(SD.weaveCreation).futureValue
        val created2 = repo.createWeave(SD.weaveCreation).futureValue
        val retrieved = repo.getWeaves.futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }

      it("should retrieve a list of weaves based on the goal they belong to") {
        val goalId = UUID.randomUUID
        val otherGoalId = UUID.randomUUID
        val created1 = repo.createWeave(SD.weaveCreation.copy(goalId = Some(goalId))).futureValue
        val created2 = repo.createWeave(SD.weaveCreation.copy(goalId = Some(goalId))).futureValue
        repo.createWeave(SD.weaveCreation.copy(goalId = Some(otherGoalId))).futureValue
        val retrieved = repo.getWeaves(goalId).futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }
    }

    describe("deleting") {
      it("should delete a weave") {
        val created = repo.createWeave(SD.weaveCreation).futureValue
        val deletion = repo.deleteWeave(created.uuid).futureValue
        val retrieved = repo.getWeave(created.uuid).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
  }

  describe("laser-donut") {
    describe("creating") {
      it("should create a laser-donut") {
        val created = repo.createLaserDonut(SD.laserDonutCreation).futureValue
        created.goalId shouldBe SD.laserDonutCreation.goalId
        created.summary shouldBe SD.laserDonutCreation.summary
        created.description shouldBe SD.laserDonutCreation.description
        created.milestone shouldBe SD.laserDonutCreation.milestone
        created.`type` shouldBe SD.laserDonutCreation.`type`
        created.status shouldBe SD.laserDonutCreation.status
      }
    }

    describe("updating") {
      it("should update a laser-donut") {
        val original = repo.createLaserDonut(SD.laserDonutCreation).futureValue
        val updated = repo.updateLaserDonut(original.uuid, SD.laserDonutUpdate).futureValue
        updated should not be original
        updated.uuid shouldBe original.uuid
        updated.goalId shouldBe SD.laserDonutUpdate.goalId.value
        updated.summary shouldBe SD.laserDonutUpdate.summary.value
        updated.description shouldBe SD.laserDonutUpdate.description.value
        updated.milestone shouldBe SD.laserDonutUpdate.milestone.value
        updated.`type` shouldBe SD.laserDonutUpdate.`type`.value
        updated.status shouldBe SD.laserDonutUpdate.status.value
      }

      it("should throw an exception if a laser-donut is not found") {
        whenReady(repo.updateLaserDonut(UUID.randomUUID, SD.laserDonutUpdate).failed) { exception =>
          exception shouldBe LaserDonutNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a laser-donut") {
        val created = repo.createLaserDonut(SD.laserDonutCreation).futureValue
        val retrieved = repo.getLaserDonut(created.uuid).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should return none if a laser-donut is not found") {
        val retrieved = repo.getLaserDonut(UUID.randomUUID).futureValue
        retrieved shouldBe empty
      }

      it("should retrieve a list of laser-donuts") {
        val created1 = repo.createLaserDonut(SD.laserDonutCreation).futureValue
        val created2 = repo.createLaserDonut(SD.laserDonutCreation).futureValue
        val retrieved = repo.getLaserDonuts.futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }

      it("should retrieve a list of laser-donuts based on the goal they belong to") {
        val goalId = UUID.randomUUID
        val otherGoalId = UUID.randomUUID
        val created1 = repo.createLaserDonut(SD.laserDonutCreation.copy(goalId = goalId)).futureValue
        val created2 = repo.createLaserDonut(SD.laserDonutCreation.copy(goalId = goalId)).futureValue
        repo.createLaserDonut(SD.laserDonutCreation.copy(goalId = otherGoalId)).futureValue
        val retrieved = repo.getLaserDonuts(goalId).futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }
    }

    describe("deleting") {
      it("should delete a laser-donuts") {
        val created = repo.createLaserDonut(SD.laserDonutCreation).futureValue
        val deletion = repo.deleteLaserDonut(created.uuid).futureValue
        val retrieved = repo.getLaserDonut(created.uuid).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
  }

  describe("updating the status of a parent laser-donut") {
    it("should modify the status to Complete, if all of it's portions are complete") {
      for {
        laserDonut <- repo.createLaserDonut(SD.laserDonutCreation.copy(status = Planned))
        firstPortion <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
        secondPortion <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
        thirdPortion <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))

        _ <- repo.updatePortion(firstPortion.uuid, SD.portionUpdate.copy(laserDonutId = None, status = Some(Complete)))
        _ <- repo.updatePortion(secondPortion.uuid, SD.portionUpdate.copy(laserDonutId = None, status = Some(Complete)))
        _ <- repo.updatePortion(thirdPortion.uuid, SD.portionUpdate.copy(laserDonutId = None, status = Some(Complete)))

        updatedLaserDonut <- repo.getLaserDonut(laserDonut.uuid)
      } yield {
        updatedLaserDonut.value.status shouldBe Complete
      }
    }

    it("should modify the status to InProgress, if some of it's portions are in-progress") {
      for {
        laserDonut <- repo.createLaserDonut(SD.laserDonutCreation.copy(status = Planned))
        firstPortion <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
        secondPortion <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
        thirdPortion <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))

        _ <- repo.updatePortion(firstPortion.uuid, SD.portionUpdate.copy(laserDonutId = None, status = Some(Planned)))
        _ <- repo.updatePortion(secondPortion.uuid, SD.portionUpdate.copy(laserDonutId = None, status = Some(InProgress)))
        _ <- repo.updatePortion(thirdPortion.uuid, SD.portionUpdate.copy(laserDonutId = None, status = Some(Planned)))

        updatedLaserDonut <- repo.getLaserDonut(laserDonut.uuid)
      } yield {
        updatedLaserDonut.value.status shouldBe InProgress
      }
    }

    it("should modify the status to InProgress, if some of it's portions are complete") {
      for {
        laserDonut <- repo.createLaserDonut(SD.laserDonutCreation.copy(status = Planned))
        firstPortion <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
        secondPortion <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
        thirdPortion <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))

        _ <- repo.updatePortion(firstPortion.uuid, SD.portionUpdate.copy(laserDonutId = None, status = Some(Planned)))
        _ <- repo.updatePortion(secondPortion.uuid, SD.portionUpdate.copy(laserDonutId = None, status = Some(Complete)))
        _ <- repo.updatePortion(thirdPortion.uuid, SD.portionUpdate.copy(laserDonutId = None, status = Some(InProgress)))

        updatedLaserDonut <- repo.getLaserDonut(laserDonut.uuid)
      } yield {
        updatedLaserDonut.value.status shouldBe InProgress
      }
    }

    it("should leave the status as Planned, if all of it's portions are planned") {
      for {
        laserDonut <- repo.createLaserDonut(SD.laserDonutCreation.copy(status = InProgress))
        firstPortion <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
        secondPortion <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
        thirdPortion <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))

        _ <- repo.updatePortion(firstPortion.uuid, SD.portionUpdate.copy(laserDonutId = None, status = Some(Planned)))
        _ <- repo.updatePortion(secondPortion.uuid, SD.portionUpdate.copy(laserDonutId = None, status = Some(Planned)))
        _ <- repo.updatePortion(thirdPortion.uuid, SD.portionUpdate.copy(laserDonutId = None, status = Some(Planned)))

        updatedLaserDonut <- repo.getLaserDonut(laserDonut.uuid)
      } yield {
        updatedLaserDonut.value.status shouldBe Planned
      }
    }
  }

  describe("portion") {
    describe("creating") {
      it("should create a portion") {
        val created = repo.createPortion(SD.portionCreation).futureValue
        created.laserDonutId shouldBe SD.portionCreation.laserDonutId
        created.summary shouldBe SD.portionCreation.summary
        created.status shouldBe SD.portionCreation.status
      }
    }

    describe("updating") {
      it("should update a portion") {
        val original = repo.createPortion(SD.portionCreation).futureValue
        val updated = repo.updatePortion(original.uuid, SD.portionUpdate).futureValue
        updated should not be original
        updated.uuid shouldBe original.uuid
        updated.laserDonutId shouldBe SD.portionUpdate.laserDonutId.value
        updated.summary shouldBe SD.portionUpdate.summary.value
        updated.status shouldBe SD.portionUpdate.status.value
      }

      it("should reorder a list of portions that belong to a specific laser-donut") {
        val laserDonutId = UUID.randomUUID
        val first = repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonutId)).futureValue
        val second = repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonutId)).futureValue
        val third = repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonutId)).futureValue
        val beforeUpdate = repo.getPortions(laserDonutId).futureValue
        val update = UpdateList(second.uuid :: third.uuid :: first.uuid :: Nil)

        repo.updatePortions(laserDonutId, update).futureValue
        val afterUpdate = repo.getPortions(laserDonutId).futureValue

        beforeUpdate should contain theSameElementsInOrderAs (first :: second :: third :: Nil)
        afterUpdate should contain theSameElementsInOrderAs (second.copy(order = 1) :: third.copy(order = 2) :: first.copy(order = 3) :: Nil)
      }

      it("should throw an exception if an invalid portion id is given in the update list") {
        val laserDonutId = UUID.randomUUID
        val first = repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonutId)).futureValue
        val second = repo.createPortion(SD.portionCreation.copy(laserDonutId = UUID.randomUUID)).futureValue
        val third = repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonutId)).futureValue
        val update = UpdateList(second.uuid :: third.uuid :: first.uuid :: Nil)
        whenReady(repo.updatePortions(laserDonutId, update).failed) { exception =>
          exception shouldBe InvalidPortionsUpdateException(s"the portions (${second.uuid}) do not belong to the laser-donut $laserDonutId")
        }
      }

      it("should throw an exception the update list is not the same as the number of portions for a laser-donut") {
        val laserDonutId = UUID.randomUUID
        val first = repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonutId)).futureValue
        val second = repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonutId)).futureValue
        repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonutId)).futureValue
        val update = UpdateList(second.uuid :: first.uuid :: Nil)
        whenReady(repo.updatePortions(laserDonutId, update).failed) { exception =>
          exception shouldBe InvalidPortionsUpdateException("the length of the update list should be the same as the number of portions for the laser-donut")
        }
      }

      it("should throw an exception if a portion is not found") {
        whenReady(repo.updatePortion(UUID.randomUUID, SD.portionUpdate).failed) { exception =>
          exception shouldBe PortionNotFoundException()
        }
      }
    }

    describe("refreshing") {
      it("should pick the next portion and update the current activity") {
        val lastWeeklyUpdate = LocalDateTime.now.minusWeeks(3).toTimestamp
        val lastDailyUpdate = LocalDateTime.now.minusDays(2).toTimestamp
        val nextDailyUpdate = LocalDateTime.now.toTimestamp
        val originalLaserDonutId = 1L
        val originalPortionId = 3L

        for {
          laserDonut <- repo.createLaserDonut(SD.laserDonutCreation)
          portion1 <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
          portion2 <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
          portion3 <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
          portion4 <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
          portion5 <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
          portion6 <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut.uuid))
          todo1 <- repo.createTodo(SD.todoCreation.copy(portionId = portion1.uuid))
          todo2 <- repo.createTodo(SD.todoCreation.copy(portionId = portion2.uuid))
          todo3 <- repo.createTodo(SD.todoCreation.copy(portionId = portion3.uuid))
          todo4 <- repo.createTodo(SD.todoCreation.copy(portionId = portion4.uuid))
          todo5 <- repo.createTodo(SD.todoCreation.copy(portionId = portion5.uuid))
          todo6 <- repo.createTodo(SD.todoCreation.copy(portionId = portion6.uuid))
          _ <- repo.insertCurrentActivity(originalLaserDonutId, originalPortionId, lastWeeklyUpdate, lastDailyUpdate)

          scheduledPortions = Seq(
            ScheduledPortion(
              id = 1L,
              uuid = portion1.uuid,
              todos = ScheduledTodo(
                uuid = todo1.uuid,
                order = todo1.order,
                status = todo1.status
              ) :: Nil,
              order = portion1.order,
              status = portion1.status
            ),
            ScheduledPortion(
              id = 2L,
              uuid = portion2.uuid,
              todos = ScheduledTodo(
                uuid = todo2.uuid,
                order = todo2.order,
                status = todo2.status
              ) :: Nil,
              order = portion2.order,
              status = portion2.status
            ),
            ScheduledPortion(
              id = 3L,
              uuid = portion3.uuid,
              todos = ScheduledTodo(
                uuid = todo3.uuid,
                order = todo3.order,
                status = todo3.status
              ) :: Nil,
              order = portion3.order,
              status = portion3.status
            ),
            ScheduledPortion(
              id = 4L,
              uuid = portion4.uuid,
              todos = ScheduledTodo(
                uuid = todo4.uuid,
                order = todo4.order,
                status = todo4.status
              ) :: Nil,
              order = portion4.order,
              status = portion4.status
            ),
            ScheduledPortion(
              id = 5L,
              uuid = portion5.uuid,
              todos = ScheduledTodo(
                uuid = todo5.uuid,
                order = todo5.order,
                status = todo5.status
              ) :: Nil,
              order = portion5.order,
              status = portion5.status
            ),
            ScheduledPortion(
              id = 6L,
              uuid = portion6.uuid,
              todos = ScheduledTodo(
                uuid = todo6.uuid,
                order = todo6.order,
                status = todo6.status
              ) :: Nil,
              order = portion6.order,
              status = portion6.status
            )
          )
          originalPortion = scheduledPortions(2)
          nextPortion = scheduledPortions(5)
          _ = when(lifeScheduler.decideNextPortion(eqTo(scheduledPortions), eqTo(Some(originalPortion)),
            eqTo(Some(lastDailyUpdate.toLocalDateTime)))).thenReturn(Some(nextPortion))
          _ = when(timer.timestampOfNow).thenReturn(nextDailyUpdate)
          _ <- repo.refreshPortion()
          currentActivity <- repo.getCurrentActivity
          currentPortion <- repo.getCurrentPortion
        } yield {
          val expectedCurrentActivity = tables.CurrentActivityRow(
            id = 1L,
            currentLaserDonut = originalLaserDonutId,
            currentPortion = nextPortion.id,
            lastDailyUpdate = nextDailyUpdate,
            lastWeeklyUpdate = lastWeeklyUpdate
          )
          currentActivity.value shouldBe expectedCurrentActivity
          currentPortion.value shouldBe portion6
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a portion") {
        val created = repo.createPortion(SD.portionCreation).futureValue
        val retrieved = repo.getPortion(created.uuid).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should return none if a portion is not found") {
        val retrieved = repo.getPortion(UUID.randomUUID).futureValue
        retrieved shouldBe empty
      }

      it("should retrieve a list of portions") {
        val created1 = repo.createPortion(SD.portionCreation).futureValue
        val created2 = repo.createPortion(SD.portionCreation).futureValue
        val retrieved = repo.getPortions.futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }

      it("should retrieve a list of portions based on the laser-donut they belong to") {
        val laserDonutId = UUID.randomUUID
        val otherLaserDonutId = UUID.randomUUID
        val created1 = repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonutId)).futureValue
        val created2 = repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonutId)).futureValue
        repo.createPortion(SD.portionCreation.copy(laserDonutId = otherLaserDonutId)).futureValue
        val retrieved = repo.getPortions(laserDonutId).futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }
    }

    describe("deleting") {
      it("should delete a portions") {
        val created = repo.createPortion(SD.portionCreation).futureValue
        val deletion = repo.deletePortion(created.uuid).futureValue
        val retrieved = repo.getPortion(created.uuid).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
  }

  describe("updating the status of a parent portion") {
    it("should modify the status to Complete, if all of it's todos are complete") {
      for {
        portion <- repo.createPortion(SD.portionCreation.copy(status = Planned))
        firstTodo <- repo.createTodo(SD.todoCreation.copy(portionId = portion.uuid))
        secondTodo <- repo.createTodo(SD.todoCreation.copy(portionId = portion.uuid))
        thirdTodo <- repo.createTodo(SD.todoCreation.copy(portionId = portion.uuid))

        _ <- repo.updateTodo(firstTodo.uuid, SD.todoUpdate.copy(portionId = None, status = Some(Complete)))
        _ <- repo.updateTodo(secondTodo.uuid, SD.todoUpdate.copy(portionId = None, status = Some(Complete)))
        _ <- repo.updateTodo(thirdTodo.uuid, SD.todoUpdate.copy(portionId = None, status = Some(Complete)))

        updatedPortion <- repo.getPortion(portion.uuid)
      } yield {
        updatedPortion.value.status shouldBe Complete
      }
    }

    it("should modify the status to InProgress, if some of it's todos are in-progress") {
      for {
        portion <- repo.createPortion(SD.portionCreation.copy(status = Planned))
        firstTodo <- repo.createTodo(SD.todoCreation.copy(portionId = portion.uuid))
        secondTodo <- repo.createTodo(SD.todoCreation.copy(portionId = portion.uuid))
        thirdTodo <- repo.createTodo(SD.todoCreation.copy(portionId = portion.uuid))

        _ <- repo.updateTodo(firstTodo.uuid, SD.todoUpdate.copy(portionId = None, status = Some(Planned)))
        _ <- repo.updateTodo(secondTodo.uuid, SD.todoUpdate.copy(portionId = None, status = Some(InProgress)))
        _ <- repo.updateTodo(thirdTodo.uuid, SD.todoUpdate.copy(portionId = None, status = Some(Planned)))

        updatedPortion <- repo.getPortion(portion.uuid)
      } yield {
        updatedPortion.value.status shouldBe InProgress
      }
    }

    it("should modify the status to Progress, if some of it's todos are complete") {
      for {
        portion <- repo.createPortion(SD.portionCreation.copy(status = Planned))
        firstTodo <- repo.createTodo(SD.todoCreation.copy(portionId = portion.uuid))
        secondTodo <- repo.createTodo(SD.todoCreation.copy(portionId = portion.uuid))
        thirdTodo <- repo.createTodo(SD.todoCreation.copy(portionId = portion.uuid))

        _ <- repo.updateTodo(firstTodo.uuid, SD.todoUpdate.copy(portionId = None, status = Some(Planned)))
        _ <- repo.updateTodo(secondTodo.uuid, SD.todoUpdate.copy(portionId = None, status = Some(Complete)))
        _ <- repo.updateTodo(thirdTodo.uuid, SD.todoUpdate.copy(portionId = None, status = Some(Planned)))

        updatedPortion <- repo.getPortion(portion.uuid)
      } yield {
        updatedPortion.value.status shouldBe InProgress
      }
    }

    it("should leave the status as InPlanned, if all of it's todos are planned") {
      for {
        portion <- repo.createPortion(SD.portionCreation.copy(status = Planned))
        firstTodo <- repo.createTodo(SD.todoCreation.copy(portionId = portion.uuid))
        secondTodo <- repo.createTodo(SD.todoCreation.copy(portionId = portion.uuid))
        thirdTodo <- repo.createTodo(SD.todoCreation.copy(portionId = portion.uuid))

        _ <- repo.updateTodo(firstTodo.uuid, SD.todoUpdate.copy(portionId = None, status = Some(Planned)))
        _ <- repo.updateTodo(secondTodo.uuid, SD.todoUpdate.copy(portionId = None, status = Some(Planned)))
        _ <- repo.updateTodo(thirdTodo.uuid, SD.todoUpdate.copy(portionId = None, status = Some(Planned)))

        updatedPortion <- repo.getPortion(portion.uuid)
      } yield {
        updatedPortion.value.status shouldBe Planned
      }
    }
  }

  describe("todo") {
    describe("creating") {
      it("should create a todo") {
        val created = repo.createTodo(SD.todoCreation).futureValue
        created.portionId shouldBe SD.todoCreation.portionId
        created.description shouldBe SD.todoCreation.description
        created.status shouldBe SD.todoCreation.status
      }
    }

    describe("updating") {
      it("should update a todo") {
        val original = repo.createTodo(SD.todoCreation).futureValue
        val updated = repo.updateTodo(original.uuid, SD.todoUpdate).futureValue
        updated should not be original
        updated.uuid shouldBe original.uuid
        updated.portionId shouldBe SD.todoUpdate.portionId.value
        updated.description shouldBe SD.todoUpdate.description.value
        updated.status shouldBe SD.todoUpdate.status.value
      }

      it("should reorder a list of todos that belong to a specific portion") {
        val portionId = UUID.randomUUID
        val first = repo.createTodo(SD.todoCreation.copy(portionId = portionId)).futureValue
        val second = repo.createTodo(SD.todoCreation.copy(portionId = portionId)).futureValue
        val third = repo.createTodo(SD.todoCreation.copy(portionId = portionId)).futureValue
        val beforeUpdate = repo.getTodos(portionId).futureValue
        val update = UpdateList(second.uuid :: third.uuid :: first.uuid :: Nil)

        repo.updateTodos(portionId, update).futureValue
        val afterUpdate = repo.getTodos(portionId).futureValue

        beforeUpdate should contain theSameElementsInOrderAs (first :: second :: third :: Nil)
        afterUpdate should contain theSameElementsInOrderAs (second.copy(order = 1) :: third.copy(order = 2) :: first.copy(order = 3) :: Nil)
      }

      it("should throw an exception if an invalid todo id is given in the update list") {
        val portionId = UUID.randomUUID
        val first = repo.createTodo(SD.todoCreation.copy(portionId = portionId)).futureValue
        val second = repo.createTodo(SD.todoCreation.copy(portionId = UUID.randomUUID)).futureValue
        val third = repo.createTodo(SD.todoCreation.copy(portionId = portionId)).futureValue
        val update = UpdateList(second.uuid :: third.uuid :: first.uuid :: Nil)
        whenReady(repo.updateTodos(portionId, update).failed) { exception =>
          exception shouldBe InvalidTodosUpdateException(s"the todos (${second.uuid}) do not belong to the portion $portionId")
        }
      }

      it("should throw an exception the update list is not the same as the number of todos for a portion") {
        val portionId = UUID.randomUUID
        val first = repo.createTodo(SD.todoCreation.copy(portionId = portionId)).futureValue
        val second = repo.createTodo(SD.todoCreation.copy(portionId = portionId)).futureValue
        repo.createTodo(SD.todoCreation.copy(portionId = portionId)).futureValue
        val update = UpdateList(second.uuid :: first.uuid :: Nil)
        whenReady(repo.updateTodos(portionId, update).failed) { exception =>
          exception shouldBe InvalidTodosUpdateException("the length of the update list should be the same as the number of todos for the portion")
        }
      }

      it("should throw an exception if a todo is not found") {
        whenReady(repo.updateTodo(UUID.randomUUID, SD.todoUpdate).failed) { exception =>
          exception shouldBe TodoNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a todo") {
        val created = repo.createTodo(SD.todoCreation).futureValue
        val retrieved = repo.getTodo(created.uuid).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should return none if a todo is not found") {
        val retrieved = repo.getTodo(UUID.randomUUID).futureValue
        retrieved shouldBe empty
      }

      it("should retrieve a list of todos") {
        val created1 = repo.createTodo(SD.todoCreation).futureValue
        val created2 = repo.createTodo(SD.todoCreation).futureValue
        val retrieved = repo.getTodos.futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }

      it("should retrieve a list of todos based on the portion they belong to") {
        val portionId = UUID.randomUUID
        val otherPortionId = UUID.randomUUID
        val created1 = repo.createTodo(SD.todoCreation.copy(portionId = portionId)).futureValue
        val created2 = repo.createTodo(SD.todoCreation.copy(portionId = portionId)).futureValue
        repo.createTodo(SD.todoCreation.copy(portionId = otherPortionId)).futureValue
        val retrieved = repo.getTodos(portionId).futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }
    }

    describe("deleting") {
      it("should delete a todos") {
        val created = repo.createTodo(SD.todoCreation).futureValue
        val deletion = repo.deleteTodo(created.uuid).futureValue
        val retrieved = repo.getTodo(created.uuid).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
  }

  describe("hobby") {
    describe("creating") {
      it("should create a hobby") {
        val created = repo.createHobby(SD.hobbyCreation).futureValue
        created.goalId shouldBe SD.hobbyCreation.goalId
        created.summary shouldBe SD.hobbyCreation.summary
        created.description shouldBe SD.hobbyCreation.description
        created.frequency shouldBe SD.hobbyCreation.frequency
        created.`type` shouldBe SD.hobbyCreation.`type`
        created.status shouldBe SD.hobbyCreation.status
      }
    }

    describe("updating") {
      it("should update a hobby") {
        val original = repo.createHobby(SD.hobbyCreation).futureValue
        val updated = repo.updateHobby(original.uuid, SD.hobbyUpdate).futureValue
        updated should not be original
        updated.uuid shouldBe original.uuid
        updated.goalId.value shouldBe SD.hobbyUpdate.goalId.value
        updated.summary shouldBe SD.hobbyUpdate.summary.value
        updated.description shouldBe SD.hobbyUpdate.description.value
        updated.frequency shouldBe SD.hobbyUpdate.frequency.value
        updated.`type` shouldBe SD.hobbyUpdate.`type`.value
        updated.status shouldBe SD.hobbyUpdate.status.value
      }

      it("should throw an exception if a hobby is not found") {
        whenReady(repo.updateHobby(UUID.randomUUID, SD.hobbyUpdate).failed) { exception =>
          exception shouldBe HobbyNotFoundException()
        }
      }
    }

    describe("retrieving") {
      it("should retrieve a hobby") {
        val created = repo.createHobby(SD.hobbyCreation).futureValue
        val retrieved = repo.getHobby(created.uuid).futureValue
        retrieved should not be empty
        retrieved.value shouldBe created
      }

      it("should return none if a hobby is not found") {
        val retrieved = repo.getHobby(UUID.randomUUID).futureValue
        retrieved shouldBe empty
      }

      it("should retrieve a list of hobbies") {
        val created1 = repo.createHobby(SD.hobbyCreation).futureValue
        val created2 = repo.createHobby(SD.hobbyCreation).futureValue
        val retrieved = repo.getHobbies.futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }

      it("should retrieve a list of hobbies based on the goal they belong to") {
        val goalId = UUID.randomUUID
        val otherGoalId = UUID.randomUUID
        val created1 = repo.createHobby(SD.hobbyCreation.copy(goalId = Some(goalId))).futureValue
        val created2 = repo.createHobby(SD.hobbyCreation.copy(goalId = Some(goalId))).futureValue
        repo.createHobby(SD.hobbyCreation.copy(goalId = Some(otherGoalId))).futureValue
        val retrieved = repo.getHobbies(goalId).futureValue
        retrieved should not be empty
        retrieved shouldBe Seq(created1, created2)
      }
    }

    describe("deleting") {
      it("should delete a hobbies") {
        val created = repo.createHobby(SD.hobbyCreation).futureValue
        val deletion = repo.deleteHobby(created.uuid).futureValue
        val retrieved = repo.getHobby(created.uuid).futureValue
        deletion shouldBe true
        retrieved shouldBe empty
      }
    }
  }

  describe("pyramid") {
    describe("creating") {
      it("should create a pyramid") {
        for {
          laserDonut1 <- repo.createLaserDonut(SD.laserDonutCreation)
          laserDonut2 <- repo.createLaserDonut(SD.laserDonutCreation)
          laserDonut3 <- repo.createLaserDonut(SD.laserDonutCreation)
          laserDonut4 <- repo.createLaserDonut(SD.laserDonutCreation)
          laserDonut5 <- repo.createLaserDonut(SD.laserDonutCreation)
          laserDonut6 <- repo.createLaserDonut(SD.laserDonutCreation)
          pyramid <- repo.createPyramidOfImportance(SD.pyramidUpsert.copy(
            tiers = SD.tierUpsert.copy(laserDonuts = (laserDonut1 :: laserDonut2 :: Nil).map(_.uuid)) ::
              SD.tierUpsert.copy(laserDonuts = (laserDonut3 :: laserDonut4 :: Nil).map(_.uuid)) ::
              SD.tierUpsert.copy(laserDonuts = (laserDonut5 :: laserDonut6 :: Nil).map(_.uuid)) :: Nil
          ))
          retrievedPyramid <- repo.getPyramidOfImportance
          scheduledLaserDonuts <- repo.getScheduledLaserDonuts
          currentLaserDonut <- repo.getCurrentLaserDonut
          currentPortion <- repo.getCurrentPortion
        } yield {
          val expectedPyramid = PyramidOfImportance(
            tiers = Tier(
              laserDonuts = laserDonut1.uuid :: laserDonut2.uuid :: Nil
            ) :: Tier(
              laserDonuts = laserDonut3.uuid :: laserDonut4.uuid :: Nil
            ) :: Tier(
              laserDonuts = laserDonut5.uuid :: laserDonut6.uuid :: Nil
            ) :: Nil,
            currentLaserDonut = None
          )
          val expectedScheduledLaserDonuts = Seq(
            tables.ScheduledLaserDonutRow(1L, 1L, 1, 0),
            tables.ScheduledLaserDonutRow(2L, 2L, 1, 0),
            tables.ScheduledLaserDonutRow(3L, 3L, 2, 0),
            tables.ScheduledLaserDonutRow(4L, 4L, 2, 0),
            tables.ScheduledLaserDonutRow(5L, 5L, 3, 0),
            tables.ScheduledLaserDonutRow(6L, 6L, 3, 0)
          )

          pyramid shouldBe expectedPyramid
          retrievedPyramid shouldBe pyramid
          scheduledLaserDonuts should contain theSameElementsInOrderAs expectedScheduledLaserDonuts
          currentLaserDonut shouldBe empty
          currentPortion shouldBe empty
        }
      }

      it("should throw an exception if a laser-donut is not found") {
        val laserDonutUuid = UUID.randomUUID
        whenReady(repo.createPyramidOfImportance(SD.pyramidUpsert.copy(
          tiers = SD.tierUpsert.copy(laserDonuts = laserDonutUuid :: Nil) :: Nil
        )).failed) { exception =>
          exception shouldBe LaserDonutNotFoundException(s"no laser-donut with the UUID $laserDonutUuid exists")
        }
      }
    }

    describe("refreshing") {
      it("should update scheduled laser-donuts and update the current activity") {
        val lastWeeklyUpdate = LocalDateTime.now.minusWeeks(3).toTimestamp
        val lastDailyUpdate = LocalDateTime.now.minusDays(2).toTimestamp
        val nextWeeklyUpdate = LocalDateTime.now.toTimestamp
        val originalLaserDonutId = 2L
        val originalPortionId = 2L

        for {
          laserDonut1 <- repo.createLaserDonut(SD.laserDonutCreation)
          laserDonut2 <- repo.createLaserDonut(SD.laserDonutCreation)
          laserDonut3 <- repo.createLaserDonut(SD.laserDonutCreation)
          laserDonut4 <- repo.createLaserDonut(SD.laserDonutCreation)
          laserDonut5 <- repo.createLaserDonut(SD.laserDonutCreation)
          laserDonut6 <- repo.createLaserDonut(SD.laserDonutCreation)
          portion1 <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut1.uuid))
          portion2 <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut2.uuid))
          portion3 <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut3.uuid))
          portion4 <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut4.uuid))
          portion5 <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut5.uuid))
          portion6 <- repo.createPortion(SD.portionCreation.copy(laserDonutId = laserDonut6.uuid))
          todo1 <- repo.createTodo(SD.todoCreation.copy(portionId = portion1.uuid))
          todo2 <- repo.createTodo(SD.todoCreation.copy(portionId = portion2.uuid))
          todo3 <- repo.createTodo(SD.todoCreation.copy(portionId = portion3.uuid))
          todo4 <- repo.createTodo(SD.todoCreation.copy(portionId = portion4.uuid))
          todo5 <- repo.createTodo(SD.todoCreation.copy(portionId = portion5.uuid))
          todo6 <- repo.createTodo(SD.todoCreation.copy(portionId = portion6.uuid))
          _ <- repo.createPyramidOfImportance(SD.pyramidUpsert.copy(
            tiers = SD.tierUpsert.copy(laserDonuts = (laserDonut1 :: laserDonut2 :: Nil).map(_.uuid)) ::
              SD.tierUpsert.copy(laserDonuts = (laserDonut3 :: laserDonut4 :: Nil).map(_.uuid)) ::
              SD.tierUpsert.copy(laserDonuts = (laserDonut5 :: laserDonut6 :: Nil).map(_.uuid)) :: Nil
          ))
          _ <- repo.insertCurrentActivity(originalLaserDonutId, originalPortionId, lastWeeklyUpdate, lastDailyUpdate)

          originalDonuts = Seq(
            ScheduledLaserDonut(
              id = 1L,
              uuid = laserDonut1.uuid,
              portions = ScheduledPortion(
                id = 1L,
                uuid = portion1.uuid,
                todos = ScheduledTodo(
                  uuid = todo1.uuid,
                  order = todo1.order,
                  status = todo1.status
                ) :: Nil,
                order = portion1.order,
                status = portion1.status
              ) :: Nil,
              tier = 1,
              status = laserDonut1.status,
              lastPerformed = None
            ),
            ScheduledLaserDonut(
              id = 2L,
              uuid = laserDonut2.uuid,
              portions = ScheduledPortion(
                id = 2L,
                uuid = portion2.uuid,
                todos = ScheduledTodo(
                  uuid = todo2.uuid,
                  order = todo2.order,
                  status = todo2.status
                ) :: Nil,
                order = portion2.order,
                status = portion2.status
              ) :: Nil,
              tier = 1,
              status = laserDonut2.status,
              lastPerformed = None
            ),
            ScheduledLaserDonut(
              id = 3L,
              uuid = laserDonut3.uuid,
              portions = ScheduledPortion(
                id = 3L,
                uuid = portion3.uuid,
                todos = ScheduledTodo(
                  uuid = todo3.uuid,
                  order = todo3.order,
                  status = todo3.status
                ) :: Nil,
                order = portion3.order,
                status = portion3.status
              ) :: Nil,
              tier = 2,
              status = laserDonut3.status,
              lastPerformed = None
            ),
            ScheduledLaserDonut(
              id = 4L,
              uuid = laserDonut4.uuid,
              portions = ScheduledPortion(
                id = 4L,
                uuid = portion4.uuid,
                todos = ScheduledTodo(
                  uuid = todo4.uuid,
                  order = todo4.order,
                  status = todo4.status
                ) :: Nil,
                order = portion4.order,
                status = portion4.status
              ) :: Nil,
              tier = 2,
              status = laserDonut4.status,
              lastPerformed = None
            ),
            ScheduledLaserDonut(
              id = 5L,
              uuid = laserDonut5.uuid,
              portions = ScheduledPortion(
                id = 5L,
                uuid = portion5.uuid,
                todos = ScheduledTodo(
                  uuid = todo5.uuid,
                  order = todo5.order,
                  status = todo5.status
                ) :: Nil,
                order = portion5.order,
                status = portion5.status
              ) :: Nil,
              tier = 3,
              status = laserDonut5.status,
              lastPerformed = None
            ),
            ScheduledLaserDonut(
              id = 6L,
              uuid = laserDonut6.uuid,
              portions = ScheduledPortion(
                id = 6L,
                uuid = portion6.uuid,
                todos = ScheduledTodo(
                  uuid = todo6.uuid,
                  order = todo6.order,
                  status = todo6.status
                ) :: Nil,
                order = portion6.order,
                status = portion6.status
              ) :: Nil,
              tier = 3,
              status = laserDonut6.status,
              lastPerformed = None
            )
          )
          refreshedDonuts = originalDonuts.tail
          originalPyramid = ScheduledPyramid(
            originalDonuts, Some(originalLaserDonutId), Some(originalPortionId), Some(lastWeeklyUpdate.toLocalDateTime)
          )
          refreshedPyramid = ScheduledPyramid(
            refreshedDonuts, Some(6L), Some(6L), Some(nextWeeklyUpdate.toLocalDateTime)
          )
          _ = when(lifeScheduler.refreshPyramid(eqTo(originalPyramid))).thenReturn(refreshedPyramid)
          _ = when(timer.timestampOfNow).thenReturn(nextWeeklyUpdate)
          _ <- repo.refreshPyramidOfImportance()
          scheduledLaserDonuts <- repo.getScheduledLaserDonuts
          currentActivity <- repo.getCurrentActivity
          currentLaserDonut <- repo.getCurrentLaserDonut
          currentPortion <- repo.getCurrentPortion
        } yield {
          val expectedScheduledLaserDonuts = Seq(
            tables.ScheduledLaserDonutRow(7L, 2L, 1, 0),
            tables.ScheduledLaserDonutRow(8L, 3L, 2, 0),
            tables.ScheduledLaserDonutRow(9L, 4L, 2, 0),
            tables.ScheduledLaserDonutRow(10, 5L, 3, 0),
            tables.ScheduledLaserDonutRow(11L, 6L, 3, 1)
          )
          val expectedCurrentActivity = tables.CurrentActivityRow(
            id = 1L,
            currentLaserDonut = 6L,
            currentPortion = 6L,
            lastDailyUpdate = lastDailyUpdate,
            lastWeeklyUpdate = nextWeeklyUpdate
          )

          scheduledLaserDonuts should contain theSameElementsInOrderAs expectedScheduledLaserDonuts
          currentActivity.value shouldBe expectedCurrentActivity
          currentLaserDonut.value shouldBe laserDonut6
          currentPortion.value shouldBe portion6
        }
      }
    }
  }
}
