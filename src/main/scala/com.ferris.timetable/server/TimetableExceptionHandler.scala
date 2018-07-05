package com.ferris.timetable.server

import akka.http.scaladsl.server.ExceptionHandler
import com.ferris.microservice.exceptions.ApiExceptions
import com.ferris.timetable.service.exceptions.Exceptions.MessageNotFoundException

object TimetableExceptionHandler {

  val handler: ExceptionHandler = ExceptionHandler {
    case e: MessageNotFoundException => throw ApiExceptions.NotFoundException("MessageNotFound", e.message, Some(ApiExceptions.NotFoundPayload("uuid")))
  }
}
