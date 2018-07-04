package com.ferris.timetable.service.exceptions

import java.util.UUID

object Exceptions {

  sealed abstract class PlanningServiceException(message: String) extends Exception(message)

  case class MessageNotFoundException(message: String = "message not found") extends PlanningServiceException(message)

  case class BacklogItemNotFoundException(message: String = "backlog-item not found") extends PlanningServiceException(message)

  case class EpochNotFoundException(message: String = "epoch not found") extends PlanningServiceException(message)

  case class YearNotFoundException(message: String = "year not found") extends PlanningServiceException(message)

  case class ThemeNotFoundException(message: String = "theme not found") extends PlanningServiceException(message)

  case class GoalNotFoundException(message: String = "goal not found") extends PlanningServiceException(message)

  case class ThreadNotFoundException(message: String = "thread not found") extends PlanningServiceException(message)

  case class WeaveNotFoundException(message: String = "weave not found") extends PlanningServiceException(message)

  case class LaserDonutNotFoundException(message: String = "laser-donut not found") extends PlanningServiceException(message)

  case class PortionNotFoundException(message: String = "portion not found") extends PlanningServiceException(message)

  case class TodoNotFoundException(message: String = "to-do not found") extends PlanningServiceException(message)

  case class HobbyNotFoundException(message: String = "hobby not found") extends PlanningServiceException(message)

  case class PyramidNotFoundException(message: String = "pyramid of importance not found") extends PlanningServiceException(message)

  case class InvalidPortionsUpdateException(message: String) extends PlanningServiceException(message)

  case class InvalidTodosUpdateException(message: String) extends PlanningServiceException(message)
}
