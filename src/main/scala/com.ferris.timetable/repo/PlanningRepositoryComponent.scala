package com.ferris.timetable.repo

import java.sql.{Date, Timestamp}
import java.util.UUID

import com.ferris.timetable.command.Commands.{CreateMessage, UpdateMessage}
import com.ferris.timetable.db.TablesComponent
import com.ferris.timetable.db.conversions.TableConversions
import com.ferris.timetable.model.Model.Message

import scala.concurrent.{ExecutionContext, Future}

trait TimetableRepositoryComponent {

  val repo: TimetableRepository

  trait TimetableRepository {
    def createMessage(creation: CreateMessage): Future[Message]

    def updateMessage(uuid: UUID, update: UpdateMessage): Future[Message]

    def getMessages: Future[Seq[Message]]

    def getMessage(uuid: UUID): Future[Option[Message]]

    def deleteMessage(uuid: UUID): Future[Boolean]
  }
}

trait SqlTimetableRepositoryComponent extends TimetableRepositoryComponent {
  this: TablesComponent =>

  lazy val tableConversions = new TableConversions(tables)
  import tableConversions.tables._
  import tableConversions.tables.profile.api._
  import tableConversions._

  implicit val repoEc: ExecutionContext
  override val repo = new SqlTimetableRepository
  val db: tables.profile.api.Database

  class SqlTimetableRepository extends TimetableRepository {

    // Create endpoints
    override def createMessage(creation: CreateMessage): Future[Message] = {
      val row = MessageRow(
        id = 0L,
        uuid = UUID.randomUUID,
        sender = creation.sender,
        content = creation.content
      )
      val action = (MessageTable returning MessageTable.map(_.id) into ((message, id) => message.copy(id = id))) += row
      db.run(action) map (row => row.asMessage)
    }

    // Update endpoints
    override def updateMessage(uuid: UUID, update: UpdateMessage): Future[Message] = {
      val query = messageByUuid(uuid).map(message => (message.sender, message.content))
      val action = getMessageAction(uuid).flatMap { maybeObj =>
        maybeObj map { old =>
          query.update(update.sender.getOrElse(old.sender), update.content.getOrElse(old.content))
            .andThen(getMessageAction(uuid).map(_.head))
        } getOrElse DBIO.failed(MessageNotFoundException())
      }.transactionally
      db.run(action).map(row => row.asMessage)
    }

    // Get endpoints
    override def getMessages: Future[Seq[Message]] = {
      db.run(MessageTable.result.map(_.map(_.asMessage)))
    }

    override def getMessage(uuid: UUID): Future[Option[Message]] = {
      db.run(getMessageAction(uuid).map(_.map(_.asMessage)))
    }

    // Delete endpoints
    override def deleteMessage(uuid: UUID): Future[Boolean] = {
      val action = messageByUuid(uuid).delete
      db.run(action).map(_ > 0)
    }

    override def deleteBacklogItem(uuid: UUID): Future[Boolean] = {
      val action = backlogItemByUuid(uuid).delete
      db.run(action).map(_ > 0)
    }

    override def deleteEpoch(uuid: UUID): Future[Boolean] = {
      val action = epochByUuid(uuid).delete
      db.run(action).map(_ > 0)
    }

    override def deleteYear(uuid: UUID): Future[Boolean] = {
      val action = yearByUuid(uuid).delete
      db.run(action).map(_ > 0)
    }

    override def deleteTheme(uuid: UUID): Future[Boolean] = {
      val action = themeByUuid(uuid).delete
      db.run(action).map(_ > 0)
    }

    override def deleteGoal(uuid: UUID): Future[Boolean] = {
      val action = getGoalAction(uuid).flatMap { maybeObj =>
        maybeObj map { case (goal, _) =>
          for {
            _ <- GoalBacklogItemTable.filter(_.goalId === goal.id).delete
            result <- GoalTable.filter(_.id === goal.id).delete
          } yield result
        } getOrElse DBIO.failed(GoalNotFoundException())
      }.transactionally
      db.run(action).map(_ > 0)
    }

    override def deleteThread(uuid: UUID): Future[Boolean] = {
      val action = threadByUuid(uuid).delete
      db.run(action).map(_ > 0)
    }

    override def deleteWeave(uuid: UUID): Future[Boolean] = {
      val action = weaveByUuid(uuid).delete
      db.run(action).map(_ > 0)
    }

    override def deleteLaserDonut(uuid: UUID): Future[Boolean] = {
      val action = laserDonutByUuid(uuid).delete
      db.run(action).map(_ > 0)
    }

    override def deletePortion(uuid: UUID): Future[Boolean] = {
      val action = portionByUuid(uuid).delete
      db.run(action).map(_ > 0)
    }

    override def deleteTodo(uuid: UUID): Future[Boolean] = {
      val action = todoByUuid(uuid).delete
      db.run(action).map(_ > 0)
    }

    override def deleteHobby(uuid: UUID): Future[Boolean] = {
      val action = hobbyByUuid(uuid).delete
      db.run(action).map(_ > 0)
    }

    // Actions
    private def insertGoalBacklogItemsAction(goalId: Long, backlogItemIds: Seq[Long]): DBIO[Seq[GoalBacklogItemRow]] = {
      val rows = backlogItemIds.map { backlogItemId =>
        GoalBacklogItemRow(
          id = 0L,
          goalId = goalId,
          backlogItemId = backlogItemId
        )
      }
      (GoalBacklogItemTable returning GoalBacklogItemTable.map(_.id) into ((goalAndItem, id) => goalAndItem.copy(id = id))) ++= rows
    }

    private def getMessageAction(uuid: UUID) = {
      messageByUuid(uuid).result.headOption
    }

    private def getBacklogItemsAction(uuids: Seq[UUID]) = {
      backlogItemsByUuid(uuids).result
    }

    private def getBacklogItemAction(uuid: UUID) = {
      backlogItemByUuid(uuid).result.headOption
    }

    private def getEpochAction(uuid: UUID) = {
      epochByUuid(uuid).result.headOption
    }

    private def getYearAction(uuid: UUID) = {
      yearByUuid(uuid).result.headOption
    }

    private def getThemeAction(uuid: UUID) = {
      themeByUuid(uuid).result.headOption
    }

    private def getGoalsAction: DBIO[Seq[(GoalRow, Seq[BacklogItemRow])]] = {
      goalsWithBacklogItems.result.map(groupByGoal)
    }

    private def getGoalAction(uuid: UUID): DBIO[Option[(GoalRow, Seq[BacklogItemRow])]] = {
      goalWithBacklogItemsByUuid(uuid).result.map(groupByGoal(_).headOption)
    }

    private def getThreadAction(uuid: UUID) = {
      threadByUuid(uuid).result.headOption
    }

    private def getWeaveAction(uuid: UUID) = {
      weaveByUuid(uuid).result.headOption
    }

    private def getLaserDonutAction(uuid: UUID) = {
      laserDonutByUuid(uuid).result.headOption
    }

    private def getPortionAction(uuid: UUID) = {
      portionByUuid(uuid).result.headOption
    }

    private def getTodoAction(uuid: UUID) = {
      todoByUuid(uuid).result.headOption
    }

    private def getHobbyAction(uuid: UUID) = {
      hobbyByUuid(uuid).result.headOption
    }

    private def getPyramidOfImportanceAction = {
      (for {
        pyramidRow <- ScheduledLaserDonutTable
        laserDonutRow <- LaserDonutTable if laserDonutRow.id === pyramidRow.laserDonutId
      } yield (pyramidRow, laserDonutRow)).result.map(_.asPyramid)
    }

    // Queries
    private def messageByUuid(uuid: UUID) = {
      MessageTable.filter(_.uuid === uuid.toString)
    }

    private def backlogItemsByUuid(uuids: Seq[UUID]) = {
      BacklogItemTable.filter(_.uuid inSet uuids.map(_.toString))
    }

    private def backlogItemByUuid(uuid: UUID) = {
      BacklogItemTable.filter(_.uuid === uuid.toString)
    }

    private def epochByUuid(uuid: UUID) = {
      EpochTable.filter(_.uuid === uuid.toString)
    }

    private def yearByUuid(uuid: UUID) = {
      YearTable.filter(_.uuid === uuid.toString)
    }

    private def themeByUuid(uuid: UUID) = {
      ThemeTable.filter(_.uuid === uuid.toString)
    }

    private def goalByUuid(uuid: UUID) = {
      GoalTable.filter(_.uuid === uuid.toString)
    }

    private def goalWithBacklogItemsByUuid(uuid: UUID) = {
      goalsWithBacklogItems.filter { case (goal, _) => goal.uuid === uuid.toString }
    }

    private def goalsWithBacklogItems = {
      GoalTable
        .joinLeft(GoalBacklogItemTable)
        .on(_.id === _.goalId)
        .joinLeft(BacklogItemTable)
        .on { case ((_, goalBacklogItem), backlogItem) => goalBacklogItem.map(_.backlogItemId).getOrElse(-1L) === backlogItem.id }
        .map { case ((goal, _), backlogItem) => (goal, backlogItem) }
    }

    private def groupByGoal(goalBacklogItems: Seq[(GoalRow, Option[BacklogItemRow])]): Seq[(GoalRow, Seq[BacklogItemRow])] = {
      goalBacklogItems.groupBy { case (goal, _) => goal }.map { case (goal, pairs) => (goal, pairs.flatMap(_._2)) }.toSeq
    }

    private def threadByUuid(uuid: UUID) = {
      ThreadTable.filter(_.uuid === uuid.toString)
    }

    private def threadsByParentId(goalId: UUID) = {
      ThreadTable.filter(_.goalId === goalId.toString)
    }

    private def weaveByUuid(uuid: UUID) = {
      WeaveTable.filter(_.uuid === uuid.toString)
    }

    private def weavesByParentId(goalId: UUID) = {
      WeaveTable.filter(_.goalId === goalId.toString)
    }

    private def laserDonutByUuid(uuid: UUID) = {
      LaserDonutTable.filter(_.uuid === uuid.toString)
    }

    private def laserDonutsByParentId(goalId: UUID) = {
      LaserDonutTable.filter(_.goalId === goalId.toString).sortBy(_.order)
    }

    private def portionByUuid(uuid: UUID) = {
      PortionTable.filter(_.uuid === uuid.toString)
    }

    private def portionsByParentId(laserDonutId: UUID) = {
      PortionTable.filter(_.laserDonutId === laserDonutId.toString).sortBy(_.order)
    }

    private def todoByUuid(uuid: UUID) = {
      TodoTable.filter(_.uuid === uuid.toString)
    }

    private def todosByParentId(portionId: UUID) = {
      TodoTable.filter(_.portionId === portionId.toString).sortBy(_.order)
    }

    private def hobbyByUuid(uuid: UUID) = {
      HobbyTable.filter(_.uuid === uuid.toString)
    }

    private def hobbiesByParentId(goalId: UUID) = {
      HobbyTable.filter(_.goalId === goalId.toString)
    }

    private def getUpdateTimes(contentUpdate: Seq[Option[Any]], statusUpdate: Seq[Option[Any]]): (Option[Timestamp], Option[Timestamp]) = {
      val now = timer.timestampOfNow
      val lastModified = if (contentUpdate.exists(_.nonEmpty)) Some(now) else None
      val lastPerformed = if (statusUpdate.exists(_.nonEmpty)) Some(now) else None
      (lastModified, lastPerformed)
    }

    private def getStatusSummary(statuses: Seq[Statuses.Status]): Statuses.Status = {
      if (statuses.forall(_ == Statuses.Complete)) Statuses.Complete
      else if (statuses.contains(Statuses.InProgress) || statuses.contains(Statuses.Complete)) Statuses.InProgress
      else Statuses.Planned
    }
  }
}
