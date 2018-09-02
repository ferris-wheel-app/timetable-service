package com.ferris.timetable.service

import java.util.UUID

import cats.data.EitherT
import com.ferris.planning.PlanningServiceComponent
import com.ferris.timetable.command.Commands._
import com.ferris.timetable.contract.resource.Resources.Out.{ConcreteBlockView, TimetableView}
import com.ferris.timetable.db.DatabaseComponent
import com.ferris.timetable.model.Model._
import com.ferris.timetable.repo.TimetableRepositoryComponent
import com.ferris.timetable.service.exceptions.Exceptions.{CurrentTemplateNotFoundException, InvalidTimetableException}
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

      def insertBuffers(timetable: Timetable) = {
        val slidingPairs = timetable.blocks.sliding(2).collect {case Seq(a, b) => (a, b)}.toSeq
        val blocksWithBuffers = slidingPairs.foldLeft(Seq.empty[ScheduledTimeBlock]) { case (aggregate, (first: ConcreteBlock, second: ConcreteBlock)) =>
          val bufferHalf = timetableConfig.bufferDuration / 2
          val bufferBlock = BufferBlock(
            start = first.finish.minusMinutes(bufferHalf),
            finish = second.start.plusMinutes(bufferHalf),
            firstTask = first.task,
            secondTask = second.task
          )
          aggregate :+ first :+ bufferBlock :+ second
        }
        timetable.copy(blocks = blocksWithBuffers)
      }

      def fetchSummary(uuid: UUID, taskType: TaskTypes.TaskType) = taskType match {
        case TaskTypes.Thread => planningService.laserDonut(uuid).map(_.map(_.summary))
        case TaskTypes.Weave => planningService.weave(uuid).map(_.map(_.summary))
        case TaskTypes.LaserDonut => planningService.laserDonut(uuid).map(_.map(_.summary))
        case TaskTypes.Hobby => planningService.hobby(uuid).map(_.map(_.summary))
      }

      def enrichWithDetails(timetable: TimetableView) = {
        timetable.blocks.map {
          case concrete: ConcreteBlockView => concrete.copy(task = concrete.task.copy(summary = fet))
        }
      }

      for {
        currentTemplate <- EitherT.fromOptionF(repo.currentTemplate, CurrentTemplateNotFoundException())
        _ <- EitherT.cond(isValidTemplate(currentTemplate), (), InvalidTimetableException("there are time-blocks without specified tasks"))
        generatedTimetable <- {
          val command = CreateTimetable(
            date = timer.today,
            blocks = currentTemplate.blocks.map { block => CreateScheduledTimeBlock(
              start = block.start,
              finish = block.finish,
              task = CreateScheduledTask(
                taskId = block.task.taskId.head,
                `type` = block.task.`type`
              )
            )}
          )
          EitherT.liftF(repo.createTimetable(command))
        }
        timetableWithBuffers <- {
          generatedTimetable.blocks.p
        }
      } yield ()
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
