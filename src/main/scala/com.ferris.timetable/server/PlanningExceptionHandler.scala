package com.ferris.timetable.server

import akka.http.scaladsl.server.ExceptionHandler
import com.ferris.microservice.exceptions.ApiExceptions
import com.ferris.planning.service.exceptions.Exceptions._

object PlanningExceptionHandler {

  val handler: ExceptionHandler = ExceptionHandler {
    case e: MessageNotFoundException => throw ApiExceptions.NotFoundException("MessageNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: BacklogItemNotFoundException => throw ApiExceptions.NotFoundException("BacklogItemNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: EpochNotFoundException => throw ApiExceptions.NotFoundException("EpochNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: YearNotFoundException => throw ApiExceptions.NotFoundException("YearNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: ThemeNotFoundException => throw ApiExceptions.NotFoundException("ThemeNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: GoalNotFoundException => throw ApiExceptions.NotFoundException("GoalNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: ThreadNotFoundException => throw ApiExceptions.NotFoundException("ThreadNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: WeaveNotFoundException => throw ApiExceptions.NotFoundException("WeaveNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: LaserDonutNotFoundException => throw ApiExceptions.NotFoundException("LaserDonutNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: PortionNotFoundException => throw ApiExceptions.NotFoundException("PortionNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: TodoNotFoundException => throw ApiExceptions.NotFoundException("TodoNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: HobbyNotFoundException => throw ApiExceptions.NotFoundException("HobbyNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
    case e: InvalidPortionsUpdateException => throw ApiExceptions.InvalidInputException("InvalidPortionsUpdate", e.getMessage)
    case e: InvalidTodosUpdateException => throw ApiExceptions.InvalidInputException("InvalidTodosUpdate", e.getMessage)
  }
}
