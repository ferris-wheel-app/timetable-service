package com.ferris.timetable.route

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.model.StatusCodes.Success
import com.ferris.timetable.contract.resource.Resources.Out.{DeletionResult, MessageView}
import com.ferris.timetable.model.Model.Message
import com.ferris.timetable.service.conversions.ModelToView._
import com.ferris.timetable.service.exceptions.Exceptions.MessageNotFoundException

trait ResponseMappings {

  def mapMessage(response: Option[Message]): (Success, MessageView) = response match {
    case Some(message) => (StatusCodes.OK, message.toView)
    case None => throw MessageNotFoundException()
  }

  def mapDeletion(deleted: Boolean): (Success, DeletionResult) =
    if (deleted) (StatusCodes.OK, DeletionResult.successful)
    else (StatusCodes.OK, DeletionResult.unsuccessful)

  def mapUpdate(updated: Boolean): (StatusCode, String) =
    if (updated) (StatusCodes.OK, "updated")
    else (StatusCodes.NotModified, "not updated")
}
