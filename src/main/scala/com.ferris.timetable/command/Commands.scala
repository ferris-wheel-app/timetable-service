package com.ferris.timetable.command

import java.time.LocalDate
import java.util.UUID

import com.ferris.planning.model.Model._

object Commands {

  case class CreateMessage(sender: String, content: String)

  case class UpdateMessage(sender: Option[String], content: Option[String])

  case class CreateBacklogItem (
    summary: String,
    description: String,
    `type`: BacklogItemTypes.BacklogItemType
  )

  case class UpdateBacklogItem (
    summary: Option[String],
    description: Option[String],
    `type`: Option[BacklogItemTypes.BacklogItemType]
  )

  case class CreateEpoch (
    name: String,
    totem: String,
    question: String
  )

  case class UpdateEpoch (
    name: Option[String],
    totem: Option[String],
    question: Option[String]
  )

  case class CreateYear (
    epochId: UUID,
    startDate: LocalDate
  )

  case class UpdateYear (
    epochId: Option[UUID],
    startDate: Option[LocalDate]
  )

  case class CreateTheme (
    yearId: UUID,
    name: String
  )

  case class UpdateTheme (
    yearId: Option[UUID],
    name: Option[String]
  )

  case class CreateGoal (
    themeId: UUID,
    backlogItems: Seq[UUID],
    summary: String,
    description: String,
    level: Int,
    priority: Boolean,
    graduation: GraduationTypes.GraduationType,
    status: GoalStatuses.GoalStatus
  )

  case class UpdateGoal (
    themeId: Option[UUID],
    backlogItems: Option[Seq[UUID]],
    summary: Option[String],
    description: Option[String],
    level: Option[Int],
    priority: Option[Boolean],
    graduation: Option[GraduationTypes.GraduationType],
    status: Option[GoalStatuses.GoalStatus]
  )

  case class CreateThread (
    goalId: Option[UUID],
    summary: String,
    description: String,
    status: Statuses.Status
  )

  case class UpdateThread (
    goalId: Option[UUID],
    summary: Option[String],
    description: Option[String],
    status: Option[Statuses.Status]
  )

  case class CreateWeave (
    goalId: Option[UUID],
    summary: String,
    description: String,
    `type`: WeaveTypes.WeaveType,
    status: Statuses.Status
  )

  case class UpdateWeave (
    goalId: Option[UUID],
    summary: Option[String],
    description: Option[String],
    `type`: Option[WeaveTypes.WeaveType],
    status: Option[Statuses.Status]
  )

  case class CreateLaserDonut (
    goalId: UUID,
    summary: String,
    description: String,
    milestone: String,
    `type`: DonutTypes.DonutType,
    status: Statuses.Status
  )

  case class UpdateLaserDonut (
    goalId: Option[UUID],
    summary: Option[String],
    description: Option[String],
    milestone: Option[String],
    `type`: Option[DonutTypes.DonutType],
    status: Option[Statuses.Status]
  )

  case class CreatePortion (
    laserDonutId: UUID,
    summary: String,
    status: Statuses.Status
  )

  case class UpdatePortion (
    laserDonutId: Option[UUID],
    summary: Option[String],
    status: Option[Statuses.Status]
  )

  case class CreateTodo (
    portionId: UUID,
    description: String,
    status: Statuses.Status
  )

  case class UpdateTodo (
    portionId: Option[UUID],
    description: Option[String],
    status: Option[Statuses.Status]
  )

  case class CreateHobby (
    goalId: Option[UUID],
    summary: String,
    description: String,
    frequency: HobbyFrequencies.HobbyFrequency,
    `type`: HobbyTypes.HobbyType,
    status: Statuses.Status
  )

  case class UpdateHobby (
    goalId: Option[UUID],
    summary: Option[String],
    description: Option[String],
    frequency: Option[HobbyFrequencies.HobbyFrequency],
    `type`: Option[HobbyTypes.HobbyType],
    status: Option[Statuses.Status]
  )

  case class UpdateList (
    reordered: Seq[UUID]
  )

  case class UpsertTier(
    laserDonuts: Seq[UUID]
  )

  case class UpsertPyramidOfImportance(
    tiers: Seq[UpsertTier]
  )
}
