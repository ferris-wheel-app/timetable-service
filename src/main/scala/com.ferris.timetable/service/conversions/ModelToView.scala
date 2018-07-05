package com.ferris.timetable.service.conversions

import com.ferris.timetable.contract.resource.Resources.Out.MessageView
import com.ferris.timetable.model.Model.Message

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
}
