package com.ferris.timetable.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{PathMatchers, Route}
import akka.stream.Materializer
import com.ferris.microservice.directive.FerrisDirectives
import com.ferris.timetable.contract.format.TimetableRestFormats
import com.ferris.timetable.contract.resource.Resources.In._
import com.ferris.timetable.service.TimetableServiceComponent
import com.ferris.timetable.service.conversions.ExternalToCommand._
import com.ferris.timetable.service.conversions.ModelToView._

import scala.concurrent.ExecutionContext

trait TimetableRoute extends FerrisDirectives with TimetableRestFormats with TimetableResponseMappings {
  this: TimetableServiceComponent =>

  implicit def routeEc: ExecutionContext
  implicit val materializer: Materializer

  private val messagesPathSegment = "messages"
  private val routinesPathSegment = "routines"
  private val timetablesPathSegment = "timetables"
  private val currentPathSegment = "current"
  private val generatePathSegment = "generate"
  private val startPathSegment = "start"

  private val createRoutine = pathPrefix(routinesPathSegment) {
    pathEndOrSingleSlash {
      post {
        entity(as[RoutineCreation]) { creation =>
          onSuccess(timetableService.createRoutine(creation.toCommand)) { response =>
            complete(StatusCodes.OK, response.toView)
          }
        }
      }
    }
  }

  private val generateTimetable = pathPrefix(timetablesPathSegment / generatePathSegment) {
    pathEndOrSingleSlash {
      post {
        onSuccess(timetableService.generateTimetable) { response =>
          complete(StatusCodes.OK, response)
        }
      }
    }
  }

  private val updateRoutineRoute = pathPrefix(routinesPathSegment / PathMatchers.JavaUUID) { id =>
    pathEndOrSingleSlash {
      put {
        entity(as[RoutineUpdate]) { update =>
          onSuccess(timetableService.updateRoutine(id, update.toCommand))(outcome => complete(mapUpdate(outcome)))
        }
      }
    }
  }

  private val startRoutineRoute = pathPrefix(routinesPathSegment / PathMatchers.JavaUUID / startPathSegment) { id =>
    pathEndOrSingleSlash {
      put {
        onSuccess(timetableService.startRoutine(id))(outcome => complete(mapUpdate(outcome)))
      }
    }
  }

  private val updateCurrentTimetableRoute = pathPrefix(timetablesPathSegment / currentPathSegment) {
    pathEndOrSingleSlash {
      put {
        entity(as[TimetableUpdate]) { update =>
          onSuccess(timetableService.updateCurrentTimetable(update.toCommand))(outcome => complete(mapUpdate(outcome)))
        }
      }
    }
  }

  private val getRoutinesRoute = pathPrefix(routinesPathSegment) {
    pathEndOrSingleSlash {
      get {
        onSuccess(timetableService.getRoutines) { response =>
          complete(StatusCodes.OK, response.map(_.toView))
        }
      }
    }
  }

  private val getRoutineRoute = pathPrefix(routinesPathSegment / PathMatchers.JavaUUID) { id =>
    pathEndOrSingleSlash {
      get {
        onSuccess(timetableService.getRoutine(id))(outcome => complete(mapRoutine(outcome)))
      }
    }
  }

  private val currentTimetable = pathPrefix(timetablesPathSegment / currentPathSegment) {
    pathEndOrSingleSlash {
      get {
        onSuccess(timetableService.currentTimetable)(outcome => complete(mapTimetable(outcome)))
      }
    }
  }

  private val deleteRoutineRoute = pathPrefix(routinesPathSegment / PathMatchers.JavaUUID) { id =>
    pathEndOrSingleSlash {
      delete {
        onSuccess(timetableService.deleteRoutine(id))(outcome => complete(mapDeletion(outcome)))
      }
    }
  }

  val timetableRoute: Route = {
    createRoutine ~
    generateTimetable ~
    updateRoutineRoute ~
    startRoutineRoute ~
    updateCurrentTimetableRoute ~
    getRoutinesRoute ~
    getRoutineRoute ~
    currentTimetable ~
    deleteRoutineRoute
  }
}
