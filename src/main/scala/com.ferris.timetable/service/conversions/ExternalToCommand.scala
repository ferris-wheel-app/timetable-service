package com.ferris.timetable.service.conversions

import com.ferris.planning.command.Commands._
import com.ferris.planning.contract.resource.Resources.In._

object ExternalToCommand {

  sealed trait CommandConversion[T] {
    def toCommand: T
  }

  implicit class MessageCreationConversion(message: MessageCreation) extends CommandConversion[CreateMessage] {
    override def toCommand = CreateMessage(
      sender = message.sender,
      content = message.content
    )
  }

  implicit class MessageUpdateConversion(message: MessageUpdate) extends CommandConversion[UpdateMessage] {
    override def toCommand = UpdateMessage(
      sender = message.sender,
      content = message.content
    )
  }

  implicit class BacklogItemCreationConversion(backlogItem: BacklogItemCreation) extends CommandConversion[CreateBacklogItem] {
    override def toCommand = CreateBacklogItem(
      summary = backlogItem.summary,
      description = backlogItem.description,
      `type` = TypeResolvers.BacklogItemType.withName(backlogItem.`type`)
    )
  }

  implicit class BacklogItemUpdateConversion(backlogItem: BacklogItemUpdate) extends CommandConversion[UpdateBacklogItem] {
    override def toCommand = UpdateBacklogItem(
      summary = backlogItem.summary,
      description = backlogItem.description,
      `type` = backlogItem.`type`.map(TypeResolvers.BacklogItemType.withName)
    )
  }

  implicit class EpochCreationConversion(epoch: EpochCreation) extends CommandConversion[CreateEpoch] {
    override def toCommand = CreateEpoch(
      name = epoch.name,
      totem = epoch.totem,
      question = epoch.question
    )
  }

  implicit class EpochUpdateConversion(epoch: EpochUpdate) extends CommandConversion[UpdateEpoch] {
    override def toCommand = UpdateEpoch(
      name = epoch.name,
      totem = epoch.totem,
      question = epoch.question
    )
  }

  implicit class YearCreationConversion(year: YearCreation) extends CommandConversion[CreateYear] {
    override def toCommand = CreateYear(
      epochId = year.epochId,
      startDate = year.startDate
    )
  }

  implicit class YearUpdateConversion(year: YearUpdate) extends CommandConversion[UpdateYear] {
    override def toCommand = UpdateYear(
      epochId = year.epochId,
      startDate = year.startDate
    )
  }

  implicit class ThemeCreationConversion(theme: ThemeCreation) extends CommandConversion[CreateTheme] {
    override def toCommand = CreateTheme(
      yearId = theme.yearId,
      name = theme.name
    )
  }

  implicit class ThemeUpdateConversion(theme: ThemeUpdate) extends CommandConversion[UpdateTheme] {
    override def toCommand = UpdateTheme(
      yearId = theme.yearId,
      name = theme.name
    )
  }

  implicit class GoalCreationConversion(goal: GoalCreation) extends CommandConversion[CreateGoal] {
    override def toCommand = CreateGoal(
      themeId = goal.themeId,
      backlogItems = goal.backlogItems,
      summary = goal.summary,
      description = goal.description,
      level = goal.level,
      priority = goal.priority,
      status = TypeResolvers.GoalStatus.withName(goal.status),
      graduation = TypeResolvers.GraduationType.withName(goal.graduation)
    )
  }

  implicit class GoalUpdateConversion(goal: GoalUpdate) extends CommandConversion[UpdateGoal] {
    override def toCommand = UpdateGoal(
      themeId = goal.themeId,
      backlogItems = goal.backlogItems,
      summary = goal.summary,
      description = goal.description,
      level = goal.level,
      priority = goal.priority,
      status = goal.status.map(TypeResolvers.GoalStatus.withName),
      graduation = goal.graduation.map(TypeResolvers.GraduationType.withName)
    )
  }

  implicit class ThreadCreationConversion(thread: ThreadCreation) extends CommandConversion[CreateThread] {
    override def toCommand = CreateThread(
      goalId = thread.goalId,
      summary = thread.summary,
      description = thread.description,
      status = TypeResolvers.Status.withName(thread.status)
    )
  }

  implicit class ThreadUpdateConversion(thread: ThreadUpdate) extends CommandConversion[UpdateThread] {
    override def toCommand = UpdateThread(
      goalId = thread.goalId,
      summary = thread.summary,
      description = thread.description,
      status = thread.status.map(TypeResolvers.Status.withName)
    )
  }

  implicit class WeaveCreationConversion(weave: WeaveCreation) extends CommandConversion[CreateWeave] {
    override def toCommand = CreateWeave(
      goalId = weave.goalId,
      summary = weave.summary,
      description = weave.description,
      status = TypeResolvers.Status.withName(weave.status),
      `type` = TypeResolvers.WeaveType.withName(weave.`type`)
    )
  }

  implicit class WeaveUpdateConversion(weave: WeaveUpdate) extends CommandConversion[UpdateWeave] {
    override def toCommand = UpdateWeave(
      goalId = weave.goalId,
      summary = weave.summary,
      description = weave.description,
      status = weave.status.map(TypeResolvers.Status.withName),
      `type` = weave.`type`.map(TypeResolvers.WeaveType.withName)
    )
  }

  implicit class LaserDonutCreationConversion(laserDonut: LaserDonutCreation) extends CommandConversion[CreateLaserDonut] {
    override def toCommand = CreateLaserDonut(
      goalId = laserDonut.goalId,
      summary = laserDonut.summary,
      description = laserDonut.description,
      milestone = laserDonut.milestone,
      status = TypeResolvers.Status.withName(laserDonut.status),
      `type` = TypeResolvers.DonutType.withName(laserDonut.`type`)
    )
  }

  implicit class LaserDonutUpdateConversion(laserDonut: LaserDonutUpdate) extends CommandConversion[UpdateLaserDonut] {
    override def toCommand = UpdateLaserDonut(
      goalId = laserDonut.goalId,
      summary = laserDonut.summary,
      description = laserDonut.description,
      milestone = laserDonut.milestone,
      status = laserDonut.status.map(TypeResolvers.Status.withName),
      `type` = laserDonut.`type`.map(TypeResolvers.DonutType.withName)
    )
  }

  implicit class PortionCreationConversion(portion: PortionCreation) extends CommandConversion[CreatePortion] {
    override def toCommand = CreatePortion(
      laserDonutId = portion.laserDonutId,
      summary = portion.summary,
      status = TypeResolvers.Status.withName(portion.status)
    )
  }

  implicit class PortionUpdateConversion(portion: PortionUpdate) extends CommandConversion[UpdatePortion] {
    override def toCommand = UpdatePortion(
      laserDonutId = portion.laserDonutId,
      summary = portion.summary,
      status = portion.status.map(TypeResolvers.Status.withName)
    )
  }

  implicit class TodoCreationConversion(todo: TodoCreation) extends CommandConversion[CreateTodo] {
    override def toCommand = CreateTodo(
      portionId = todo.portionId,
      description = todo.description,
      status = TypeResolvers.Status.withName(todo.status)
    )
  }

  implicit class TodoUpdateConversion(todo: TodoUpdate) extends CommandConversion[UpdateTodo] {
    override def toCommand = UpdateTodo(
      portionId = todo.portionId,
      description = todo.description,
      status = todo.status.map(TypeResolvers.Status.withName)
    )
  }

  implicit class HobbyCreationConversion(hobby: HobbyCreation) extends CommandConversion[CreateHobby] {
    override def toCommand = CreateHobby(
      goalId = hobby.goalId,
      summary = hobby.summary,
      description = hobby.description,
      frequency = TypeResolvers.HobbyFrequency.withName(hobby.frequency),
      status = TypeResolvers.Status.withName(hobby.status),
      `type` = TypeResolvers.HobbyType.withName(hobby.`type`)
    )
  }

  implicit class HobbyUpdateConversion(hobby: HobbyUpdate) extends CommandConversion[UpdateHobby] {
    override def toCommand = UpdateHobby(
      goalId = hobby.goalId,
      summary = hobby.summary,
      description = hobby.description,
      frequency = hobby.frequency.map(TypeResolvers.HobbyFrequency.withName),
      status = hobby.status.map(TypeResolvers.Status.withName),
      `type` = hobby.`type`.map(TypeResolvers.HobbyType.withName)
    )
  }

  implicit class ListUpdateConversion(list: ListUpdate) extends CommandConversion[UpdateList] {
    override def toCommand = UpdateList(
      reordered = list.reordered
    )
  }

  implicit class PyramidOfImportanceUpsertConversion(pyramid: PyramidOfImportanceUpsert) extends CommandConversion[UpsertPyramidOfImportance] {
    override def toCommand = UpsertPyramidOfImportance(
      tiers = pyramid.tiers.map(tier => UpsertTier(tier.laserDonuts))
    )
  }
}
