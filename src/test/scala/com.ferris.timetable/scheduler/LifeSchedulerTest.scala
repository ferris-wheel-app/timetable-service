package com.ferris.timetable.scheduler

import java.time.LocalDateTime

import com.ferris.planning.config.PlanningServiceConfig
import com.ferris.planning.model.Model.ScheduledPyramid
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.OptionValues._
import com.ferris.planning.model.Model.Statuses._
import com.ferris.planning.sample.SampleData.domain._
import com.ferris.planning.utils.MockTimerComponent
import com.ferris.planning.utils.PlanningImplicits._
import org.mockito.Mockito._

class LifeSchedulerTest extends FunSpec with Matchers {

  type ScheduleContext = DefaultLifeSchedulerComponent with MockTimerComponent
  def newContext(acceptableProgress: Int = 100): ScheduleContext = {
    val config = PlanningServiceConfig(acceptableProgress)
    new DefaultLifeSchedulerComponent with MockTimerComponent {
      override val lifeScheduler: DefaultLifeScheduler = new DefaultLifeScheduler(config)
    }
  }

  describe("a life-scheduler") {
    it("should leave a pyramid unchanged, if a week has not passed since it was last updated") {
      val context = newContext()
      val previousUpdate = LocalDateTime.now
      val nextUpdate = previousUpdate.plusDays(3)
      val pyramid = scheduledPyramid.copy(
        laserDonuts = scheduledLaserDonut :: scheduledLaserDonut :: Nil,
        lastUpdate = Some(previousUpdate)
      )
      when(context.timer.now).thenReturn(nextUpdate.toLong)

      context.lifeScheduler.refreshPyramid(pyramid) shouldBe pyramid
    }

    it("should leave a pyramid unchanged, if there are no scheduled laser-donuts") {
      val context = newContext()
      val pyramid = scheduledPyramid.copy(
        laserDonuts = Nil,
        lastUpdate = None
      )

      context.lifeScheduler.refreshPyramid(pyramid) shouldBe pyramid
    }

    describe("performing a shift") {
      it("should perform a shift, when there are completed laser-donuts") {
        val context = newContext()
        val previousUpdate = LocalDateTime.now
        val nextUpdate = previousUpdate.plusDays(7)

        val d = scheduledLaserDonut
        val topTier = d.copy(id = 1, tier = 1, status = Planned) :: d.copy(id = 2, tier = 1, status = Complete) :: d.copy(id = 3, tier = 1, status = Complete) :: Nil
        val tier2 = d.copy(id = 4, tier = 2, status = Planned) :: d.copy(id = 5, tier = 2, status = Planned) :: d.copy(id = 6, tier = 2, status = Planned) :: Nil
        val tier3 = d.copy(id = 7, tier = 3, status = Planned) :: d.copy(id = 8, tier = 3, status = Planned) :: d.copy(id = 9, tier = 3, status = Planned) :: Nil
        val originalPyramid = scheduledPyramid.copy(
          laserDonuts = topTier ++ tier2 ++ tier3,
          lastUpdate = Some(previousUpdate)
        )
        when(context.timer.now).thenReturn(nextUpdate.toLong)

        val updatedPyramid = context.lifeScheduler.refreshPyramid(originalPyramid)
        val shiftedDonuts = updatedPyramid.laserDonuts
        val newTopTier = shiftedDonuts.filter(_.tier == 1)
        val newTier2 = shiftedDonuts.filter(_.tier == 2)
        val newTier3 = shiftedDonuts.filter(_.tier == 3)

        shiftedDonuts.size shouldBe originalPyramid.laserDonuts.size - 2

        newTopTier.size shouldBe 3
        newTopTier should contain(topTier.head)
        newTopTier.count(donut => tier2.map(_.copy(tier = 1)).contains(donut)) shouldBe 2

        newTier2.size shouldBe 3
        newTier2.count(donut => tier3.map(_.copy(tier = 2)).contains(donut)) shouldBe 2

        newTier3.size shouldBe 1
      }

      it("should perform a shift, when there are no completed laser-donuts, but there has been acceptable progress") {
        val context = newContext(60)
        val previousUpdate = LocalDateTime.now
        val nextUpdate = previousUpdate.plusDays(7)

        val todos = (1 to 6).map(_ => scheduledTodo.copy(status = Complete)) ++
          (1 to 2).map(_ => scheduledTodo.copy(status = InProgress)) ++
          (1 to 2).map(_ => scheduledTodo.copy(status = Planned))
        val portions = (1 to 12).map(_ => scheduledPortion.copy(todos = todos))
        val d = scheduledLaserDonut.copy(status = Planned, portions = portions)

        val topTier = d.copy(id = 1, tier = 1) :: d.copy(id = 2, tier = 1) :: d.copy(id = 3, tier = 1) :: Nil
        val tier2 = d.copy(id = 4, tier = 2) :: d.copy(id = 5, tier = 2) :: d.copy(id = 6, tier = 2) :: Nil
        val tier3 = d.copy(id = 7, tier = 3) :: d.copy(id = 8, tier = 3) :: d.copy(id = 9, tier = 3) :: Nil
        val originalPyramid = scheduledPyramid.copy(
          laserDonuts = topTier ++ tier2 ++ tier3,
          lastUpdate = Some(previousUpdate)
        )
        when(context.timer.now).thenReturn(nextUpdate.toLong)

        val updatedPyramid = context.lifeScheduler.refreshPyramid(originalPyramid)
        val shiftedDonuts = updatedPyramid.laserDonuts
        val newTopTier = shiftedDonuts.filter(_.tier == 1)
        val newTier2 = shiftedDonuts.filter(_.tier == 2)
        val newTier3 = shiftedDonuts.filter(_.tier == 3)

        shiftedDonuts.size shouldBe originalPyramid.laserDonuts.size

        newTopTier.size shouldBe 4
        newTopTier should contain(topTier.head)
        newTopTier.count(donut => tier2.map(_.copy(tier = 1)).contains(donut)) shouldBe 1

        newTier2.size shouldBe 3
        newTier2.count(donut => tier3.map(_.copy(tier = 2)).contains(donut)) shouldBe 1

        newTier3.size shouldBe 2
      }

      it("should not perform a shift, if there are neither completed laser-donuts nor an acceptable progress") {
        val context = newContext(60)
        val previousUpdate = LocalDateTime.now
        val nextUpdate = previousUpdate.plusDays(7)

        val todos = (1 to 4).map(_ => scheduledTodo.copy(status = Complete)) ++
          (1 to 3).map(_ => scheduledTodo.copy(status = InProgress)) ++
          (1 to 3).map(_ => scheduledTodo.copy(status = Planned))
        val portions = (1 to 12).map(_ => scheduledPortion.copy(todos = todos))
        val d = scheduledLaserDonut.copy(status = Planned, portions = portions)

        val topTier = d.copy(id = 1, tier = 1) :: d.copy(id = 2, tier = 1) :: d.copy(id = 3, tier = 1) :: Nil
        val tier2 = d.copy(id = 4, tier = 2) :: d.copy(id = 5, tier = 2) :: d.copy(id = 6, tier = 2) :: Nil
        val tier3 = d.copy(id = 7, tier = 3) :: d.copy(id = 8, tier = 3) :: d.copy(id = 9, tier = 3) :: Nil
        val originalPyramid = scheduledPyramid.copy(
          laserDonuts = topTier ++ tier2 ++ tier3,
          lastUpdate = Some(previousUpdate)
        )
        when(context.timer.now).thenReturn(nextUpdate.toLong)

        val updatedPyramid = context.lifeScheduler.refreshPyramid(originalPyramid)
        updatedPyramid.laserDonuts shouldBe originalPyramid.laserDonuts
      }
    }

    describe("choosing the next laser-donut") {
      it("should leave the current laser-donut unchanged, if there are no laser-donuts") {
        val context = newContext()
        val pyramid = scheduledPyramid.copy(
          laserDonuts = Nil,
          lastUpdate = None
        )

        context.lifeScheduler.refreshPyramid(pyramid).currentLaserDonut shouldBe pyramid.currentLaserDonut
      }

      it("should choose a planned laser-donut, if there are some that are present") {
        val context = newContext()
        val previousUpdate = LocalDateTime.now
        val nextUpdate = previousUpdate.plusDays(7)

        val d = scheduledLaserDonut
        val topTier = d.copy(id = 1, tier = 1, status = Planned) :: d.copy(id = 2, tier = 1, status = Planned) :: d.copy(id = 3, tier = 1, status = InProgress) :: Nil
        val tier2 = d.copy(id = 4, tier = 2, status = Planned) :: d.copy(id = 5, tier = 2, status = Planned) :: d.copy(id = 6, tier = 2, status = Planned) :: Nil
        val tier3 = d.copy(id = 7, tier = 3, status = Planned) :: d.copy(id = 8, tier = 3, status = Planned) :: d.copy(id = 9, tier = 3, status = Planned) :: Nil
        val originalPyramid: ScheduledPyramid = scheduledPyramid.copy(
          laserDonuts = topTier ++ tier2 ++ tier3,
          lastUpdate = Some(previousUpdate)
        )
        when(context.timer.now).thenReturn(nextUpdate.toLong)

        val updatedPyramid = context.lifeScheduler.refreshPyramid(originalPyramid)
        val currentLaserDonut = updatedPyramid.currentLaserDonut

        Seq(currentLaserDonut.value) should contain oneElementOf topTier.map(_.id)
      }

      it("should choose a laser-donut that was tackled the least recently") {
        val context = newContext()
        val previousUpdate = LocalDateTime.now
        val nextUpdate = previousUpdate.plusDays(7)

        val d = scheduledLaserDonut
        val threeWeeksAgo = d.copy(id = 1, tier = 1, status = InProgress, lastPerformed = Some(LocalDateTime.now.minusWeeks(3)))
        val twoWeeksAgo = d.copy(id = 1, tier = 1, status = InProgress, lastPerformed = Some(LocalDateTime.now.minusWeeks(2)))
        val oneWeekAgo = d.copy(id = 1, tier = 1, status = InProgress, lastPerformed = Some(LocalDateTime.now.minusWeeks(1)))
        val topTier = threeWeeksAgo :: twoWeeksAgo :: oneWeekAgo :: Nil
        val tier2 = d.copy(id = 4, tier = 2, status = Planned) :: d.copy(id = 5, tier = 2, status = Planned) :: d.copy(id = 6, tier = 2, status = Planned) :: Nil
        val tier3 = d.copy(id = 7, tier = 3, status = Planned) :: d.copy(id = 8, tier = 3, status = Planned) :: d.copy(id = 9, tier = 3, status = Planned) :: Nil
        val originalPyramid: ScheduledPyramid = scheduledPyramid.copy(
          laserDonuts = topTier ++ tier2 ++ tier3,
          lastUpdate = Some(previousUpdate)
        )
        when(context.timer.now).thenReturn(nextUpdate.toLong)

        val updatedPyramid = context.lifeScheduler.refreshPyramid(originalPyramid)
        val currentLaserDonut = updatedPyramid.currentLaserDonut

        currentLaserDonut.value shouldBe threeWeeksAgo.id
      }

      it("should choose a laser-donut that has the least percentage of its portions tackled") {
        val context = newContext()
        val previousUpdate = LocalDateTime.now
        val nextUpdate = previousUpdate.plusDays(7)

        val lowestPerformedTodos = (1 to 6).map(_ => scheduledTodo.copy(status = Complete)) ++
          (1 to 2).map(_ => scheduledTodo.copy(status = InProgress)) ++
          (1 to 2).map(_ => scheduledTodo.copy(status = Planned))
        val mediumPerformedTodos = (1 to 7).map(_ => scheduledTodo.copy(status = Complete)) ++
          (1 to 3).map(_ => scheduledTodo.copy(status = InProgress))
        val highestPerformedTodos = (1 to 8).map(_ => scheduledTodo.copy(status = Complete)) ++
          (1 to 2).map(_ => scheduledTodo.copy(status = Planned))

        val lowestPerformedPortions = (1 to 12).map(_ => scheduledPortion.copy(todos = lowestPerformedTodos))
        val mediumPerformedPortions = (1 to 12).map(_ => scheduledPortion.copy(todos = mediumPerformedTodos))
        val highestPerformedPortions = (1 to 12).map(_ => scheduledPortion.copy(todos = highestPerformedTodos))

        val lowestPerformedLaserDonut = scheduledLaserDonut.copy(id = 1, tier = 1, status = InProgress, portions = lowestPerformedPortions)
        val mediumPerformedLaserDonut = scheduledLaserDonut.copy(id = 2, tier = 1, status = InProgress, portions = mediumPerformedPortions)
        val highestPerformedLaserDonut = scheduledLaserDonut.copy(id = 3, tier = 1, status = InProgress, portions = highestPerformedPortions)
        val d = scheduledLaserDonut

        val topTier = lowestPerformedLaserDonut :: mediumPerformedLaserDonut :: highestPerformedLaserDonut :: Nil
        val tier2 = d.copy(id = 4, tier = 2) :: d.copy(id = 5, tier = 2) :: d.copy(id = 6, tier = 2) :: Nil
        val tier3 = d.copy(id = 7, tier = 3) :: d.copy(id = 8, tier = 3) :: d.copy(id = 9, tier = 3) :: Nil
        val originalPyramid: ScheduledPyramid = scheduledPyramid.copy(
          laserDonuts = topTier ++ tier2 ++ tier3,
          lastUpdate = Some(previousUpdate)
        )
        when(context.timer.now).thenReturn(nextUpdate.toLong)

        val updatedPyramid = context.lifeScheduler.refreshPyramid(originalPyramid)
        val currentLaserDonut = updatedPyramid.currentLaserDonut

        currentLaserDonut.value shouldBe lowestPerformedLaserDonut.id
      }
    }

    describe("choosing the next portion") {
      it("should return the default portion, if a day has not passed since the current portion was last chosen") {
        val context = newContext()
        val previousUpdate = LocalDateTime.now
        val nextUpdate = previousUpdate.plusHours(12)

        val firstPortion = scheduledPortion.copy(id = 1)
        val secondPortion = scheduledPortion.copy(id = 2)
        val thirdPortion = scheduledPortion.copy(id = 3)
        val defaultPortion = scheduledPortion.copy(id = 4)
        val portions = firstPortion :: secondPortion :: thirdPortion :: Nil

        when(context.timer.now).thenReturn(nextUpdate.toLong)

        val nextPortion = context.lifeScheduler.decideNextPortion(portions, Some(defaultPortion), Some(previousUpdate))

        nextPortion.value shouldBe defaultPortion
      }

      it("should choose nothing as the next portion, if there are no portions and the default portion is completed") {
        val context = newContext()
        val previousUpdate = LocalDateTime.now
        val nextUpdate = previousUpdate.plusHours(24)

        when(context.timer.now).thenReturn(nextUpdate.toLong)

        val nextPortion = context.lifeScheduler.decideNextPortion(Nil, None, Some(previousUpdate))

        nextPortion shouldBe None
      }

      it("should choose the default portion as the next portion, if there are no portions") {
        val context = newContext()
        val previousUpdate = LocalDateTime.now
        val nextUpdate = previousUpdate.plusHours(24)
        val defaultPortion = scheduledPortion.copy(id = 4)

        when(context.timer.now).thenReturn(nextUpdate.toLong)

        val nextPortion = context.lifeScheduler.decideNextPortion(Nil, Some(defaultPortion), Some(previousUpdate))

        nextPortion.value shouldBe defaultPortion
      }

      it("should choose the default portion as the next portion, if all portions are complete") {
        val context = newContext()
        val previousUpdate = LocalDateTime.now
        val nextUpdate = previousUpdate.plusHours(24)

        val defaultPortion = scheduledPortion.copy(id = 4)
        val portions = (1 to 5).map(_ => scheduledPortion.copy(status = Complete))

        when(context.timer.now).thenReturn(nextUpdate.toLong)

        val nextPortion = context.lifeScheduler.decideNextPortion(portions, Some(defaultPortion), Some(previousUpdate))

        nextPortion.value shouldBe defaultPortion
      }

      it("should choose the first portion, if all the portions are planned") {
        val context = newContext()
        val previousUpdate = LocalDateTime.now
        val nextUpdate = previousUpdate.plusHours(24)

        val defaultPortion = scheduledPortion.copy(id = 4)
        val portions = (1 to 5).map(_ => scheduledPortion.copy(status = Planned))

        when(context.timer.now).thenReturn(nextUpdate.toLong)

        val nextPortion = context.lifeScheduler.decideNextPortion(portions, Some(defaultPortion), Some(previousUpdate))

        nextPortion.value shouldBe portions.head
      }

      it("should choose the first portion that is in progress, if there are some that are present") {
        val context = newContext()
        val previousUpdate = LocalDateTime.now
        val nextUpdate = previousUpdate.plusHours(24)

        val firstPortion = scheduledPortion.copy(id = 1, status = Complete)
        val secondPortion = scheduledPortion.copy(id = 2, status = InProgress)
        val thirdPortion = scheduledPortion.copy(id = 3, status = Planned)
        val fourthPortion = scheduledPortion.copy(id = 4, status = InProgress)
        val defaultPortion = scheduledPortion.copy(id = 5)
        val portions = firstPortion :: secondPortion :: thirdPortion :: fourthPortion :: Nil

        when(context.timer.now).thenReturn(nextUpdate.toLong)

        val nextPortion = context.lifeScheduler.decideNextPortion(portions, Some(defaultPortion), Some(previousUpdate))

        nextPortion.value shouldBe secondPortion
      }
    }
  }
}
