package com.ferris.timetable.service

import java.util.UUID

import cats.data.EitherT
import cats.implicits._
import com.ferris.planning.PlanningServiceComponent
import com.ferris.timetable.command.Commands._
import com.ferris.timetable.contract.resource.Resources.Out._
import com.ferris.timetable.db.DatabaseComponent
import com.ferris.timetable.model.Model._
import com.ferris.timetable.repo.TimetableRepositoryComponent
import com.ferris.timetable.service.conversions.ModelToView._
import com.ferris.timetable.service.exceptions.Exceptions.{CurrentTemplateNotFoundException, InvalidTimetableException, TimetableServiceException}
import com.ferris.utils.TimerComponent

import scala.concurrent.{ExecutionContext, Future}

trait TimetableServiceComponent {
  val timetableService: TimetableService

  trait TimetableService {
    def createMessage(creation: CreateMessage)(implicit ex: ExecutionContext): Future[Message]
    def createRoutine(routine: CreateRoutine)(implicit ex: ExecutionContext): Future[Routine]
    def generateTimetable(implicit ex: ExecutionContext): Future[TimetableView]

    def updateMessage(uuid: UUID, update: UpdateMessage)(implicit ex: ExecutionContext): Future[Message]
    def updateRoutine(uuid: UUID, update: UpdateRoutine)(implicit ex: ExecutionContext): Future[Boolean]
    def startRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean]
    def updateTimetable(update: UpdateTimetable): Future[Boolean]

    def getMessages(implicit ex: ExecutionContext): Future[Seq[Message]]
    def getRoutines(implicit ex: ExecutionContext): Future[Seq[Routine]]

    def getMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[Message]]
    def getRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[Routine]]
    def currentTimetable(implicit ex: ExecutionContext): Future[Option[Timetable]]

    def deleteMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean]
    def deleteRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean]
  }
}

trait DefaultTimetableServiceComponent extends TimetableServiceComponent {
  this: TimetableRepositoryComponent with DatabaseComponent with TimerComponent with PlanningServiceComponent =>

  override val timetableService = new DefaultTimetableService(DefaultTimetableConfig.apply)

  class DefaultTimetableService(timetableConfig: TimetableConfig) extends TimetableService {

    override def createMessage(creation: CreateMessage)(implicit ex: ExecutionContext): Future[Message] = {
      db.run(repo.createMessage(creation))
    }

    override def createRoutine(routine: CreateRoutine)(implicit ex: ExecutionContext): Future[Routine] = {
      db.run(repo.createRoutine(routine))
    }

    override def generateTimetable(implicit ex: ExecutionContext): Future[TimetableView] = {
      def isValidTemplate(template: TimetableTemplate) = {
        template.blocks.filterNot(_.task.`type` == TaskTypes.LaserDonut).forall(_.task.taskId.nonEmpty)
      }

      def fillEmptySlots(blocks: Seq[TimeBlockTemplate]) = Future.sequence {
        blocks.collect {
          case laserBlock @ TimeBlockTemplate(_, _, TaskTemplate(None, TaskTypes.LaserDonut)) =>
            planningService.currentPortion.map { currentPortion =>
              laserBlock.copy(task = TaskTemplate(currentPortion.map(_.uuid), TaskTypes.LaserDonut))
            }
          case otherBlock => Future.successful(otherBlock)
        }
      }

      def insertBuffers(timetable: Timetable) = {
        val slidingPairs = timetable.blocks.sliding(2).collect { case Seq(a, b) => (a, b) }.toSeq
        val blocksWithBuffers = slidingPairs.foldLeft(timetable.blocks.head :: Nil) { case (aggregate, (first: ConcreteBlock, second: ConcreteBlock)) =>
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

    override def updateMessage(uuid: UUID, update: UpdateMessage)(implicit ex: ExecutionContext): Future[Message] = {
      db.run(repo.updateMessage(uuid, update))
    }

    override def updateRoutine(uuid: UUID, update: UpdateRoutine)(implicit ex: ExecutionContext): Future[Boolean] = {
      db.run(repo.updateRoutine(uuid, update))
    }

    override def startRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean] = {
      db.run(repo.startRoutine(uuid))
    }

    override def updateTimetable(update: UpdateTimetable): Future[Boolean] = {
      db.run(repo.updateTimetable(update))
    }

    override def getMessages(implicit ex: ExecutionContext): Future[Seq[Message]] = {
      db.run(repo.getMessages)
    }

    override def getMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Option[Message]] = {
      db.run(repo.getMessage(uuid))
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

    override def deleteMessage(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean] = {
      db.run(repo.deleteMessage(uuid))
    }

    override def deleteRoutine(uuid: UUID)(implicit ex: ExecutionContext): Future[Boolean] = {
      db.run(repo.deleteRoutine(uuid))
    }
  }
}
