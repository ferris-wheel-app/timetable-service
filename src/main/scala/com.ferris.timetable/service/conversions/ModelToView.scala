package com.ferris.timetable.service.conversions

import com.ferris.timetable.contract.resource.Resources.Out._
import com.ferris.timetable.model.Model._

object ModelToView {

  implicit class MessageConversion(message: Message) {
    def toView: MessageView = {
      MessageView(
        uuid = message.uuid,
        sender = message.sender,
        content = message.content
      )
    }
  }

  implicit class ConcreteTimeBlockConversion(timeBlock: ConcreteBlock) {
    def toView: TimeBlockView = {
      ConcreteBlockView(
        uuid = timeBlock.uuid,
        start = timeBlock.start,
        finish = timeBlock.finish,
        task = timeBlock.task
      )
    }
  }
}
