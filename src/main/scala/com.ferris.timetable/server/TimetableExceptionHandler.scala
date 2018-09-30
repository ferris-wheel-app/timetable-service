package com.ferris.timetable.server

import akka.http.scaladsl.server.ExceptionHandler
import com.ferris.microservice.exceptions.ApiExceptions
import com.ferris.timetable.service.exceptions.Exceptions._

object TimetableExceptionHandler {

  val handler: ExceptionHandler = ExceptionHandler {
    case e: MessageNotFoundException => throw ApiExceptions.NotFoundException("MessageNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: RoutineNotFoundException => throw ApiExceptions.NotFoundException("RoutineNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: CurrentTemplateNotFoundException => throw ApiExceptions.NotFoundException("CurrentTemplateNotFound", e.message, None)
    case e: TimetableNotFoundException => throw ApiExceptions.NotFoundException("TimetableNotFound", e.message, None)
    case e: InvalidTimetableException => throw ApiExceptions.InvalidInputException("InvalidTimetable", e.message)
  }
}
