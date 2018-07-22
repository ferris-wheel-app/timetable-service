package com.ferris.timetable.client

import java.util.UUID

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Path._
import akka.stream.ActorMaterializer
import com.ferris.microservice.resource.DeletionResult
import com.ferris.service.client.{HttpServer, ServiceClient}
import com.ferris.timetable.contract.format.TimetableRestFormats
import com.ferris.timetable.contract.resource.Resources.In.{MessageCreation, MessageUpdate}
import com.ferris.timetable.contract.resource.Resources.Out.MessageView

import scala.concurrent.Future

class TimetableServiceClient(val server: HttpServer, implicit val mat: ActorMaterializer) extends ServiceClient with TimetableRestFormats {

  def this(server: HttpServer) = this(server, server.mat)

  private val apiPath = /("api")

  private val messagesPath = "messages"

  def createMessage(creation: MessageCreation): Future[MessageView] =
    makePostRequest[MessageCreation, MessageView](Uri(path = apiPath / messagesPath), creation)

  def updateMessage(id: UUID, update: MessageUpdate): Future[MessageView] =
    makePutRequest[MessageUpdate, MessageView](Uri(path = apiPath / messagesPath / id.toString), update)

  def message(id: UUID): Future[Option[MessageView]] =
    makeGetRequest[Option[MessageView]](Uri(path = apiPath / messagesPath / id.toString))

  def messages: Future[List[MessageView]] =
    makeGetRequest[List[MessageView]](Uri(path = apiPath / messagesPath))

  def deleteMessage(id: UUID): Future[DeletionResult] =
    makeDeleteRequest[DeletionResult](Uri(path = apiPath / messagesPath / id.toString))
}
