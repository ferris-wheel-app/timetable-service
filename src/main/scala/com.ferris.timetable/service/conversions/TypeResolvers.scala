package com.ferris.timetable.service.conversions

import com.ferris.planning.contract.resource.TypeFields
import com.ferris.planning.model.Model.BacklogItemTypes.BacklogItemType
import com.ferris.planning.model.Model.DonutTypes.DonutType
import com.ferris.planning.model.Model.GoalStatuses.GoalStatus
import com.ferris.planning.model.Model.GraduationTypes.GraduationType
import com.ferris.planning.model.Model.HobbyFrequencies.HobbyFrequency
import com.ferris.planning.model.Model.HobbyTypes.HobbyType
import com.ferris.planning.model.Model.Statuses.Status
import com.ferris.planning.model.Model.WeaveTypes.WeaveType
import com.ferris.planning.model.Model._

object TypeResolvers {

  sealed trait TypeResolver[T <: TypeEnum] {
    def withName(name: String): T
    def toString(`type`: T): String
  }

  object BacklogItemType extends TypeResolver[BacklogItemTypes.BacklogItemType] {
    import TypeFields.BacklogItemType._

    override def withName(name: String): BacklogItemType = name match {
      case `idea` => BacklogItemTypes.Idea
      case `issue` => BacklogItemTypes.Issue
      case o => throw new IllegalArgumentException(s"Invalid backlog-item type: $o")
    }

    override def toString(`type`: BacklogItemType): String = `type` match {
      case BacklogItemTypes.Idea => idea
      case BacklogItemTypes.Issue => issue
      case o => throw new IllegalArgumentException(s"Invalid backlog-item type: $o")
    }
  }

  object Status extends TypeResolver[Statuses.Status] {
    import TypeFields.Status._

    override def withName(name: String): Status = name match {
      case `planned` => Statuses.Planned
      case `inProgress` => Statuses.InProgress
      case `complete` => Statuses.Complete
      case o => throw new IllegalArgumentException(s"Invalid status: $o")
    }

    override def toString(`type`: Status): String = `type` match {
      case Statuses.Planned => planned
      case Statuses.InProgress => inProgress
      case Statuses.Complete => complete
      case o => throw new IllegalArgumentException(s"Invalid status: $o")
    }
  }

  object GoalStatus extends TypeResolver[GoalStatuses.GoalStatus] {
    import TypeFields.GoalStatus._

    override def withName(name: String): GoalStatus = name match {
      case `notAchieved` => GoalStatuses.NotAchieved
      case `employed` => GoalStatuses.Employed
      case `unemployed` => GoalStatuses.Unemployed
      case o => throw new IllegalArgumentException(s"Invalid goal status: $o")
    }

    override def toString(`type`: GoalStatus): String = `type` match {
      case GoalStatuses.NotAchieved => notAchieved
      case GoalStatuses.Employed => employed
      case GoalStatuses.Unemployed => unemployed
      case o => throw new IllegalArgumentException(s"Invalid goal status: $o")
    }
  }

  object GraduationType extends TypeResolver[GraduationTypes.GraduationType] {
    import TypeFields.GraduationType._

    override def withName(name: String): GraduationType = name match {
      case `abandoned` => GraduationTypes.Abandoned
      case `thread` => GraduationTypes.Thread
      case `weave` => GraduationTypes.Weave
      case `hobby` => GraduationTypes.Hobby
      case `goal` => GraduationTypes.Goal
      case o => throw new IllegalArgumentException(s"Invalid graduation type: $o")
    }

    override def toString(`type`: GraduationType): String = `type` match {
      case GraduationTypes.Abandoned => abandoned
      case GraduationTypes.Thread => thread
      case GraduationTypes.Weave => weave
      case GraduationTypes.Hobby => hobby
      case GraduationTypes.Goal => goal
      case o => throw new IllegalArgumentException(s"Invalid graduation type: $o")
    }
  }

  object DonutType extends TypeResolver[DonutTypes.DonutType] {
    import TypeFields.DonutType._

    override def withName(name: String): DonutType = name match {
      case `projectFocused` => DonutTypes.ProjectFocused
      case `skillFocused` => DonutTypes.SkillFocused
      case o => throw new IllegalArgumentException(s"Invalid donut type: $o")
    }

    override def toString(`type`: DonutType): String = `type` match {
      case DonutTypes.ProjectFocused => projectFocused
      case DonutTypes.SkillFocused => skillFocused
      case o => throw new IllegalArgumentException(s"Invalid donut type: $o")
    }
  }

  object WeaveType extends TypeResolver[WeaveTypes.WeaveType] {
    import TypeFields.WeaveType._

    override def withName(name: String): WeaveType = name match {
      case `priority` => WeaveTypes.Priority
      case `pdr` => WeaveTypes.PDR
      case `bau` => WeaveTypes.BAU
      case o => throw new IllegalArgumentException(s"Invalid weave type: $o")
    }

    override def toString(`type`: WeaveType): String = `type` match {
      case WeaveTypes.Priority => priority
      case WeaveTypes.PDR => pdr
      case WeaveTypes.BAU => bau
      case o => throw new IllegalArgumentException(s"Invalid weave type: $o")
    }
  }

  object HobbyType extends TypeResolver[HobbyTypes.HobbyType] {
    import TypeFields.HobbyType._

    override def withName(name: String): HobbyType = name match {
      case `active` => HobbyTypes.Active
      case `passive` => HobbyTypes.Passive
      case o => throw new IllegalArgumentException(s"Invalid hobby type: $o")
    }

    override def toString(`type`: HobbyType): String = `type` match {
      case HobbyTypes.Active => active
      case HobbyTypes.Passive => passive
      case o => throw new IllegalArgumentException(s"Invalid hobby type: $o")
    }
  }

  object HobbyFrequency extends TypeResolver[HobbyFrequencies.HobbyFrequency] {
    import TypeFields.HobbyFrequency._

    override def withName(name: String): HobbyFrequency = name match {
      case `oneOff` => HobbyFrequencies.OneOff
      case `continuous` => HobbyFrequencies.Continuous
      case o => throw new IllegalArgumentException(s"Invalid hobby frequency: $o")
    }

    override def toString(`type`: HobbyFrequency): String = `type` match {
      case HobbyFrequencies.OneOff => oneOff
      case HobbyFrequencies.Continuous => continuous
      case o => throw new IllegalArgumentException(s"Invalid hobby frequency: $o")
    }
  }
}
