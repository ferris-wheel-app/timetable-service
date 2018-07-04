package com.ferris.timetable.route

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.model.StatusCodes.Success
import com.ferris.planning.contract.resource.Resources.Out._
import com.ferris.planning.model.Model._
import com.ferris.planning.service.conversions.ModelToView._
import com.ferris.planning.service.exceptions.Exceptions._

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
