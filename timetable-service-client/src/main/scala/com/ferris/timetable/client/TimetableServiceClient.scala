package com.ferris.timetable.client

import akka.stream.ActorMaterializer
import com.ferris.service.client.{HttpServer, ServiceClient}
import com.ferris.timetable.contract.format.TimetableRestFormats

class TimetableServiceClient(val server: HttpServer, implicit val mat: ActorMaterializer) extends ServiceClient with TimetableRestFormats {

  def this(server: HttpServer) = this(server, server.mat)
}
