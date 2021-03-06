package com.ferris.timetable.service.exceptions

object Exceptions {

  sealed abstract class TimetableServiceException(message: String) extends Exception(message)

  case class RoutineNotFoundException(message: String = "routine not found") extends TimetableServiceException(message)

  case class CurrentTemplateNotFoundException(message: String = "no current template found") extends TimetableServiceException(message)

  case class TimetableNotFoundException(message: String = "timetable not found") extends TimetableServiceException(message)

  case class InvalidTimetableException(message: String) extends TimetableServiceException(message)

  case class UnknownServiceError(message: String) extends TimetableServiceException(message)
}
