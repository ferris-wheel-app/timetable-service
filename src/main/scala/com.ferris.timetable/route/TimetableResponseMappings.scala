package com.ferris.timetable.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.Success
import com.ferris.microservice.route.FerrisResponseMappings
import com.ferris.timetable.contract.resource.Resources.Out.MessageView
import com.ferris.timetable.model.Model.Message
import com.ferris.timetable.service.conversions.ModelToView._
import com.ferris.timetable.service.exceptions.Exceptions.MessageNotFoundException

trait TimetableResponseMappings extends FerrisResponseMappings {

  def mapMessage(response: Option[Message]): (Success, MessageView) = response match {
    case Some(message) => (StatusCodes.OK, message.toView)
    case None => throw MessageNotFoundException()
  }
}
