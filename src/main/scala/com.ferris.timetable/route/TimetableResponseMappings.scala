package com.ferris.timetable.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.Success
import com.ferris.microservice.route.FerrisResponseMappings
import com.ferris.timetable.contract.resource.Resources.Out._
import com.ferris.timetable.model.Model._
import com.ferris.timetable.service.conversions.ModelToView._
import com.ferris.timetable.service.exceptions.Exceptions._

trait TimetableResponseMappings extends FerrisResponseMappings {

  def mapRoutine(response: Option[Routine]): (Success, RoutineView) = response match {
    case Some(routine) => (StatusCodes.OK, routine.toView)
    case None => throw RoutineNotFoundException()
  }

  def mapTemplate(response: Option[TimetableTemplate]): (Success, TimetableTemplateView) = response match {
    case Some(template) => (StatusCodes.OK, template.toView)
    case None => throw CurrentTemplateNotFoundException()
  }

  def mapTimetable(response: Option[Timetable]): (Success, TimetableView) = response match {
    case Some(timetable) => (StatusCodes.OK, timetable.toView)
    case None => throw TimetableNotFoundException()
  }
}
