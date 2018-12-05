package com.ferris.timetable.service

import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.UUID

import cats.data.EitherT
import cats.implicits._
import com.ferris.planning.PlanningServiceComponent
import com.ferris.planning.contract.resource.Resources.Out.{OneOffView, ScheduledOneOffView}
import com.ferris.timetable.command.Commands._
import com.ferris.timetable.contract.resource.Resources.Out._
import com.ferris.timetable.db.DatabaseComponent
import com.ferris.timetable.model.Model._
import com.ferris.timetable.repo.TimetableRepositoryComponent
import com.ferris.timetable.service.conversions.ModelToView._
import com.ferris.timetable.service.exceptions.Exceptions.{CurrentTemplateNotFoundException, InvalidTimetableException, TimetableServiceException}
import com.ferris.timetable.utils.TimetableUtils
import com.ferris.utils.FerrisImplicits._
import com.ferris.utils.TimerComponent

import scala.concurrent.{ExecutionContext, Future}
import scala.math.abs

trait TimetableServiceComponent {
  val timetableService: TimetableService

  trait TimetableService {
    def createRoutine(routine: CreateRoutine)(implicit ex: ExecutionContext): Future[Routine]
    def generateTimetable(implicit ex: ExecutionContext): Future[TimetableView]

    def updateRoutine(uuid: UUID, update: UpdateRoutine)(implicit ex: ExecutionContext): Future[Boolean]
    def startRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean]
    def updateCurrentTimetable(update: UpdateTimetable)(implicit ex: ExecutionContext): Future[Boolean]

    def getRoutines(implicit ex: ExecutionContext): Future[Seq[Routine]]

    def getRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[Routine]]
    def currentTimetable(implicit ex: ExecutionContext): Future[Option[Timetable]]

    def deleteRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean]
  }
}

trait DefaultTimetableServiceComponent extends TimetableServiceComponent {
  this: TimetableRepositoryComponent with DatabaseComponent with TimerComponent with PlanningServiceComponent with TimetableUtils =>

  override val timetableService = new DefaultTimetableService(DefaultTimetableConfig.apply)

  class DefaultTimetableService(timetableConfig: TimetableConfig) extends TimetableService {

    override def createRoutine(routine: CreateRoutine)(implicit ex: ExecutionContext): Future[Routine] = {
      db.run(repo.createRoutine(routine))
    }

    override def generateTimetable(implicit ex: ExecutionContext): Future[TimetableView] = {
      def isValidTemplate(template: TimetableTemplate) = {
        template.blocks.filterNot(_.task.`type` == TaskTypes.LaserDonut).forall(_.task.taskId.nonEmpty)
      }

      def integrateOneOffs(blocks: Seq[TimeBlockTemplate], oneOffs: Seq[OneOffView]): Seq[TimeBlockTemplate] = {
        (blocks, oneOffs) match {
          case (Nil, Nil) => blocks
          case (Nil, event :: _) => throw InvalidTimetableException(s"there needs to be a one-off slot of ${getDurationHms(event.estimate)}")
          case (slot :: slots, events) if slot.task.taskId.nonEmpty => Seq(slot) ++ integrateOneOffs(slots, events)
          case (slot :: slots, event :: events) if slot.durationInMillis <= event.estimate =>
            Seq(slot.copy(task = TaskTemplate(Some(event.uuid), TaskTypes.OneOff))) ++ integrateOneOffs(slots, events)
          case (slot :: slots, event :: events) if slot.durationInMillis > event.estimate =>
            val filledInFirstSlot = slot.copy(task = TaskTemplate(Some(event.uuid), TaskTypes.OneOff))
            val leftOverSlot = TimeBlockTemplate(
              start = slot.start.plus(event.estimate, ChronoUnit.MILLIS),
              finish = slot.finish,
              task = TaskTemplate(
                taskId = None,
                `type` = TaskTypes.OneOff
              )
            )
            Seq(filledInFirstSlot) ++ integrateOneOffs(Seq(leftOverSlot) ++ slots, events)
          case (_, Nil) => throw InvalidTimetableException(s"no one-off slot needed, since there are no existing one-off events")
        }
      }

      def getStartAndFinish(scheduledOneOff: ScheduledOneOffView): (LocalTime, LocalTime) = {
        val scheduledEventStart = scheduledOneOff.occursOn.toLocalTime
        (scheduledEventStart, scheduledEventStart.plus(scheduledOneOff.estimate, ChronoUnit.MILLIS))
      }

      def calculateGaps(scheduledOneOff: ScheduledOneOffView, block: TimeBlockTemplate): (Long, Long) = {
        val (scheduledEventStart, scheduledEventFinish) = getStartAndFinish(scheduledOneOff)
        (abs(block.start.toLong - scheduledEventStart.toLong), abs(block.finish.toLong - scheduledEventFinish.toLong))
      }

      def isLostCause(block: TimeBlockTemplate, scheduledChunk: Long): Boolean = {
        ((block.durationInMillis.toDouble / scheduledChunk) * 100) <= 50
      }

      def occursBefore(scheduledOneOff: ScheduledOneOffView, block: TimeBlockTemplate): Boolean = {
        val (_, scheduledEventFinish) = getStartAndFinish(scheduledOneOff)
        scheduledEventFinish.isBefore(block.start)
      }

      def occursAfter(scheduledOneOff: ScheduledOneOffView, block: TimeBlockTemplate): Boolean = {
        val (scheduledEventStart, _) = getStartAndFinish(scheduledOneOff)
        scheduledEventStart.isAfter(block.finish)
      }

      def occursOverFirstHalf(scheduledOneOff: ScheduledOneOffView, block: TimeBlockTemplate): Boolean = {
        val (scheduledEventStart, scheduledEventFinish) = getStartAndFinish(scheduledOneOff)
        (scheduledEventStart.isBefore(block.start) || scheduledEventStart == block.start) &&
          scheduledEventFinish.isBefore(block.finish)
      }

      def occursOverLastHalf(scheduledOneOff: ScheduledOneOffView, block: TimeBlockTemplate): Boolean = {
        val (scheduledEventStart, scheduledEventFinish) = getStartAndFinish(scheduledOneOff)
        scheduledEventStart.isAfter(block.start) &&
          (scheduledEventFinish == block.finish || scheduledEventFinish.isAfter(block.finish))
      }

      def occursWithin(scheduledOneOff: ScheduledOneOffView, block: TimeBlockTemplate): Boolean = {
        val (scheduledEventStart, scheduledEventFinish) = getStartAndFinish(scheduledOneOff)
        (scheduledEventStart == block.start) || scheduledEventStart.isAfter(block.start) &&
          (scheduledEventFinish == block.finish || scheduledEventFinish.isBefore(block.finish))
      }

      def convertToSlot(scheduledOneOff: ScheduledOneOffView, gapBefore: Long = 0L, gapAfter: Long = 0L): TimeBlockTemplate = {
        val (eventStart, eventEnd) = getStartAndFinish(scheduledOneOff)
        TimeBlockTemplate(
          start = eventStart.minus(gapBefore, ChronoUnit.MILLIS),
          finish = eventEnd.plus(gapAfter, ChronoUnit.MILLIS),
          task = TaskTemplate(
            taskId = Some(scheduledOneOff.uuid),
            `type` = TaskTypes.ScheduledOneOff
          )
        )
      }

      def integrateScheduledOneOffs(blocks: Seq[TimeBlockTemplate], scheduledOneOffs: Seq[ScheduledOneOffView]): Seq[TimeBlockTemplate] = {
        ((blocks, scheduledOneOffs) match {
          case (_, Nil) | (Nil, _) => blocks
          case (slot :: slots, event :: _) if occursAfter(event, slot) => Seq(slot) ++ integrateScheduledOneOffs(slots, scheduledOneOffs)
          case (slot :: slots, event :: events) if occursWithin(event, slot) =>
            val (gapBefore, gapAfter) = calculateGaps(event, slot)
            val eventSlot = convertToSlot(event, gapBefore = gapBefore, gapAfter = gapAfter)
            Seq(eventSlot) ++ integrateScheduledOneOffs(slots, events)
          case (slot :: slots, event :: _) if occursOverLastHalf(event, slot) =>
            val (eventStart, _) = getStartAndFinish(event)
            val (gapBefore, _) = calculateGaps(event, slot)
            val eventSlots = {
              if (isLostCause(slot, gapBefore)) Seq(convertToSlot(event, gapBefore = gapBefore))
              else Seq(slot.copy(finish = eventStart), convertToSlot(event))
            }
            eventSlots ++ integrateScheduledOneOffs(slots, scheduledOneOffs)
          case (slot :: slots, event :: events) if occursOverFirstHalf(event, slot) =>
            val (_, eventFinish) = getStartAndFinish(event)
            val (_, gapAfter) = calculateGaps(event, slot)
            val eventSlots = {
              if (isLostCause(slot, gapAfter)) Seq(convertToSlot(event, gapAfter = gapAfter))
              else Seq(convertToSlot(event), slot.copy(start = eventFinish))
            }
            eventSlots ++ integrateScheduledOneOffs(slots, events)
          case (slot :: slots, event :: events) if occursBefore(event, slot) => Seq(slot) ++ integrateScheduledOneOffs(slots, events)
        }).distinct
      }

      def fillOneOffSlots(blocks: Seq[TimeBlockTemplate]): Future[Seq[TimeBlockTemplate]] = {
        import com.ferris.planning.contract.resource.TypeFields.Status

        if(List(DayOfTheWeek.Saturday, DayOfTheWeek.Sunday).contains(dayOfTheWeek)) {
          for {
            oneOffs <- planningService.oneOffs
            relevantOneOffs = oneOffs.filter(oneOff => List(Status.inProgress, Status.planned).contains(oneOff.status))
          } yield integrateOneOffs(blocks, relevantOneOffs)
        } else Future.successful(blocks)
      }

      def fillScheduledOneOffSlots(blocks: Seq[TimeBlockTemplate]): Future[Seq[TimeBlockTemplate]] = {
        planningService.scheduledOneOffs(Some(timer.today)).map(integrateScheduledOneOffs(blocks, _))
      }

      def fillLaserDonutSlots(blocks: Seq[TimeBlockTemplate]): Future[Seq[TimeBlockTemplate]] = Future.sequence {
        blocks.collect {
          case laserBlock @ TimeBlockTemplate(_, _, TaskTemplate(None, TaskTypes.LaserDonut)) =>
            planningService.currentPortion.map { currentPortion =>
              laserBlock.copy(task = TaskTemplate(currentPortion.map(_.uuid), TaskTypes.LaserDonut))
            }
          case otherBlock => Future.successful(otherBlock)
        }
      }

      def fillEmptySlots(blocks: Seq[TimeBlockTemplate]) = {
        for {
          withLaserDonuts <- fillLaserDonutSlots(blocks)
          withOneOffs <- fillOneOffSlots(withLaserDonuts)
          withScheduledOneOffs <- fillScheduledOneOffSlots(withOneOffs)
        } yield withScheduledOneOffs
      }

      def insertBuffers(timetable: Timetable) = {
        val slidingPairs = timetable.blocks.sliding(2).collect { case Seq(a, b) => (a, b) }.toSeq
        val blocksWithBuffers = slidingPairs.foldLeft(timetable.blocks.headOption.toSeq) { case (aggregate, (first: ConcreteBlock, second: ConcreteBlock)) =>
          val bufferHalf = timetableConfig.bufferDuration / 2
          val bufferBlock = BufferBlock(
            start = first.finish.minusMinutes(bufferHalf),
            finish = second.start.plusMinutes(bufferHalf),
            firstTask = first.task,
            secondTask = second.task
          )
          aggregate :+ bufferBlock :+ second
        }
        timetable.copy(blocks = blocksWithBuffers)
      }

      def fetchSummary(uuid: UUID, taskType: TaskTypes.TaskType) = taskType match {
        case TaskTypes.Thread => planningService.thread(uuid).map(_.map(_.summary))
        case TaskTypes.Weave => planningService.weave(uuid).map(_.map(_.summary))
        case TaskTypes.LaserDonut => planningService.portion(uuid).map(_.map(_.summary))
        case TaskTypes.Hobby => planningService.hobby(uuid).map(_.map(_.summary))
        case TaskTypes.OneOff => planningService.oneOff(uuid).map(_.map(_.description))
        case TaskTypes.ScheduledOneOff => planningService.scheduledOneOff(uuid).map(_.map(_.description))
      }

      def taskView(task: ScheduledTask) = {
        fetchSummary(task.taskId, task.`type`).map(summary => task.toView.copy(summary = summary))
      }

      def timeBlockView(timeBlock: ScheduledTimeBlock) = timeBlock match {
        case concreteBlock: ConcreteBlock => taskView(concreteBlock.task).map(taskView => concreteBlock.toView.copy(task = taskView))
        case bufferBlock: BufferBlock =>
          for {
            firstTaskView <- taskView(bufferBlock.firstTask)
            secondTaskView <- taskView(bufferBlock.secondTask)
          } yield bufferBlock.toView.copy(firstTask = firstTaskView, secondTask = secondTaskView)
      }

      def timetableView(timetable: Timetable) = {
        for {
          timeBlockViews <- Future.sequence(timetable.blocks.map(timeBlockView))
        } yield timetable.toView.copy(blocks = timeBlockViews)
      }

      def fromFuture[T](futureResult: Future[T]): EitherT[Future, TimetableServiceException, T] =
        EitherT(futureResult.map(Right(_): Either[TimetableServiceException, T]).recover {
          case exception: TimetableServiceException => Left(exception)
        })

      (for {
        currentTemplate <- EitherT.fromOptionF(db.run(repo.currentTemplate), CurrentTemplateNotFoundException())
        _ <- EitherT.cond[Future](isValidTemplate(currentTemplate), (), InvalidTimetableException("there are time-blocks without specified tasks"))
        filledInBlocks <- fromFuture(fillEmptySlots(currentTemplate.blocks))
        generatedTimetable <- {
          val command = CreateTimetable(
            date = timer.today,
            blocks = filledInBlocks.map { block => CreateScheduledTimeBlock(
              start = block.start,
              finish = block.finish,
              task = CreateScheduledTask(
                taskId = block.task.taskId.head,
                `type` = block.task.`type`
              )
            )}
          )
          fromFuture(db.run(repo.createTimetable(command)))
        }
        timetableWithBuffers = insertBuffers(generatedTimetable)
        timetableView <- fromFuture(timetableView(timetableWithBuffers))
      } yield timetableView).value.map {
        case Right(newTimetable) => newTimetable
        case Left(error) => throw error
      }
    }

    override def updateRoutine(uuid: UUID, update: UpdateRoutine)(implicit ex: ExecutionContext): Future[Boolean] = {
      db.run(repo.updateRoutine(uuid, update))
    }

    override def startRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean] = {
      db.run(repo.startRoutine(uuid))
    }

    override def updateCurrentTimetable(update: UpdateTimetable)(implicit ex: ExecutionContext): Future[Boolean] = {
      db.run(repo.updateTimetable(update))
    }

    override def getRoutines(implicit ex: ExecutionContext): Future[Seq[Routine]] = {
      db.run(repo.getRoutines)
    }

    override def getRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[Routine]] = {
      db.run(repo.getRoutine(uuid))
    }

    override def currentTimetable(implicit ex: ExecutionContext): Future[Option[Timetable]] = {
      db.run(repo.currentTimetable)
    }

    override def deleteRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean] = {
      db.run(repo.deleteRoutine(uuid))
    }
  }
}
