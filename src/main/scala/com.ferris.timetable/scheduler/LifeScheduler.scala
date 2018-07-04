package com.ferris.timetable.scheduler

import java.time.LocalDateTime

import com.ferris.planning.config.{DefaultPlanningServiceConfig, PlanningServiceConfig}
import com.ferris.planning.model.Model._
import com.ferris.planning.model.Model.Statuses._
import com.ferris.planning.utils.PlanningImplicits._
import com.ferris.planning.utils.TimerComponent

import scala.util.Random

trait LifeSchedulerComponent {

  def lifeScheduler: LifeScheduler

  trait LifeScheduler {
    def refreshPyramid(pyramid: ScheduledPyramid): ScheduledPyramid
    def decideNextPortion(portions: Seq[ScheduledPortion], defaultChoice: Option[ScheduledPortion], lastUpdate: Option[LocalDateTime]): Option[ScheduledPortion]
  }
}

trait DefaultLifeSchedulerComponent extends LifeSchedulerComponent {
  this: TimerComponent =>

  override def lifeScheduler = new DefaultLifeScheduler(DefaultPlanningServiceConfig.apply)

  class DefaultLifeScheduler(config: PlanningServiceConfig) extends LifeScheduler {

    private val ONE_DAY: Long = 1000 * 60 * 60 * 24
    private val ONE_WEEK: Long = ONE_DAY * 7

    private type Criteria = Seq[ScheduledLaserDonut] => Seq[ScheduledLaserDonut]

    override def refreshPyramid(pyramid: ScheduledPyramid): ScheduledPyramid = {
      def getPortions(laserDonut: Option[ScheduledLaserDonut]): Seq[ScheduledPortion] = {
        laserDonut.map(_.portions.sortBy(_.order)).getOrElse(Nil)
      }

      def decideNextLaserDonut(laserDonuts: Seq[ScheduledLaserDonut]): Option[ScheduledLaserDonut] = {
        laserDonuts.partition(_.status == Planned) match {
          case (Nil, Nil) => None
          case (Nil, inProgress) => chooseRandom(applyCriteria(inProgress, Seq(timeRule, progressRule)))
          case (planned, _) => chooseRandom(planned)
        }
      }

      def aWeekHasPassed(lastUpdate: LocalDateTime): Boolean = {
        (timer.now - lastUpdate.toLong) >= ONE_WEEK
      }

      pyramid.laserDonuts match {
        case _ if pyramid.lastUpdate.nonEmpty && !aWeekHasPassed(pyramid.lastUpdate.get) => pyramid
        case Nil => pyramid
        case _ =>
          val (topTier, bottomTiers) = pyramid.laserDonuts.partition(_.tier == 1)
          val (completed, leftOvers) = topTier.partition(_.status == Complete)
          val shiftSize = completed match {
            case Nil if acceptableProgress(topTier) => 1
            case _ => completed.size
          }
          val shifted = leftOvers ++ shift(bottomTiers, shiftSize)
          val (newTopTier, newBottomTiers) = shifted.partition(_.tier == 1)
          val currentLaserDonut = decideNextLaserDonut(newTopTier)
          val currentPortion = decideNextPortion(getPortions(currentLaserDonut), None, None)
          ScheduledPyramid(newTopTier ++ newBottomTiers, currentLaserDonut.map(_.id), currentPortion.map(_.id), None)
      }
    }

    override def decideNextPortion(portions: Seq[ScheduledPortion], defaultChoice: Option[ScheduledPortion], lastDailyUpdate: Option[LocalDateTime]): Option[ScheduledPortion] = {
      def aDayHasPassed(lastUpdate: LocalDateTime): Boolean = {
        (timer.now - lastUpdate.toLong) >= ONE_DAY
      }

      def defaultOrBust = if (defaultChoice.exists(_.status == Complete)) None else defaultChoice

      if (lastDailyUpdate.nonEmpty && !aDayHasPassed(lastDailyUpdate.get))
        defaultChoice
      else {
        val planned = portions.filter(_.status == Planned)
        val inProgress = portions.filter(_.status == InProgress)
        (planned, inProgress) match {
          case (Nil, Nil) => defaultOrBust
          case (_, Nil) => planned.headOption
          case (_, _) => inProgress.headOption
        }
      }
    }

    private def timeRule(laserDonuts: Seq[ScheduledLaserDonut]): Seq[ScheduledLaserDonut] = {
      laserDonuts.groupBy(donut => timer.now - donut.lastPerformed.head.toLong)
        .toSeq.sortBy(_._1).reverse.map(_._2).headOption.getOrElse(Nil)
    }

    private def progressRule(laserDonuts: Seq[ScheduledLaserDonut]): Seq[ScheduledLaserDonut] = {
      laserDonuts.groupBy(progressPercentage).toSeq.sortBy(_._1).map(_._2).headOption.getOrElse(Nil)
    }

    private def applyCriteria(laserDonuts: Seq[ScheduledLaserDonut], rules: Seq[Criteria]): Seq[ScheduledLaserDonut] = {
      rules.headOption match {
        case None => laserDonuts
        case Some(rule) => rule(laserDonuts) match {
          case Nil => laserDonuts
          case leftOvers => applyCriteria(leftOvers, rules.tail)
        }
      }
    }

    private def chooseRandom[T](list: Seq[T]): Option[T] = {
      list match {
        case Nil => None
        case head :: Nil => Some(head)
        case _ => Some(list(Random.nextInt(list.length)))
      }
    }

    private def shift(laserDonuts: Seq[ScheduledLaserDonut], steps: Int): Seq[ScheduledLaserDonut] = {
      require(steps >= 0)

      def shiftByOne(list: Seq[ScheduledLaserDonut]) = {
        list.map(donut => donut.copy(tier = donut.tier - 1))
      }

      def shift(laserDonuts: Seq[ScheduledLaserDonut], steps: Int, startingTier: Int): Seq[ScheduledLaserDonut] = {
        steps match {
          case 0 => laserDonuts
          case _ => laserDonuts match {
            case smallList if smallList.size <= steps => shiftByOne(smallList)
            case bigList =>
              val (currentTier, tiersBelow) = bigList.partition(_.tier == startingTier)
              val (toBeShifted, toBeIgnored) = currentTier.splitAt(steps)
              shiftByOne(toBeShifted) ++ toBeIgnored ++ shift(tiersBelow, steps, startingTier + 1)
          }
        }
      }

      shift(laserDonuts, steps, 1)
    }

    private def acceptableProgress(laserDonuts: Seq[ScheduledLaserDonut]): Boolean = {
      laserDonuts.forall(progressPercentage(_) >= config.acceptableProgress)
    }

    private def progressPercentage(laserDonut: ScheduledLaserDonut): Int = {
      val all = laserDonut.portions.flatMap(_.todos)
      val total = all.size
      val completed: Double = all.count(_.status == Statuses.Complete)
      ((completed / total) * 100).toInt
    }
  }
}
