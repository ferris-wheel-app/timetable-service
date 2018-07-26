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
  private val templatesPathSegment = "templates"
  private val timetablesPathSegment = "timetables"
  private val currentPathSegment = "current"
  private val generatePathSegment = "generate"

  private val createMessageRoute = pathPrefix(messagesPathSegment) {
    pathEndOrSingleSlash {
      post {
        entity(as[MessageCreation]) { creation =>
          onSuccess(timetableService.createMessage(creation.toCommand)) { response =>
            complete(StatusCodes.OK, response.toView)
          }
        }
      }
    }
  }

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

  private val createTemplate = pathPrefix(timetablesPathSegment / templatesPathSegment) {
    pathEndOrSingleSlash {
      post {
        entity(as[TimetableTemplateCreation]) { creation =>
          onSuccess(timetableService.createTemplate(creation.toCommand)) { response =>
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
          complete(StatusCodes.OK, response.toView)
        }
      }
    }
  }

  private val updateMessageRoute = pathPrefix(messagesPathSegment / PathMatchers.JavaUUID) { id =>
    pathEndOrSingleSlash {
      put {
        entity(as[MessageUpdate]) { update =>
          onSuccess(timetableService.updateMessage(id, update.toCommand)) { response =>
            complete(StatusCodes.OK, response.toView)
          }
        }
      }
    }
  }

  private val getMessagesRoute = pathPrefix(messagesPathSegment) {
    pathEndOrSingleSlash {
      get {
        onSuccess(timetableService.getMessages) { response =>
          complete(StatusCodes.OK, response.map(_.toView))
        }
      }
    }
  }

  private val getMessageRoute = pathPrefix(messagesPathSegment / PathMatchers.JavaUUID) { id =>
    pathEndOrSingleSlash {
      get {
        onSuccess(timetableService.getMessage(id))(outcome => complete(mapMessage(outcome)))
      }
    }
  }

  private val deleteMessageRoute = pathPrefix(messagesPathSegment / PathMatchers.JavaUUID) { id =>
    pathEndOrSingleSlash {
      delete {
        onSuccess(timetableService.deleteMessage(id))(outcome => complete(mapDeletion(outcome)))
      }
    }
  }

  val timetableRoute: Route = {
    createMessageRoute ~
    updateMessageRoute ~
    getMessagesRoute ~
    getMessageRoute ~
    deleteMessageRoute
  }
}
