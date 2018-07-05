package com.ferris.timetable.sample

import java.time.LocalDate
import java.util.UUID

import com.ferris.timetable.contract.resource.Resources.In._
import com.ferris.timetable.contract.resource.Resources.Out._
import com.ferris.timetable.model.Model._
import com.ferris.timetable.command.Commands.{CreateMessage, UpdateMessage}

object SampleData {

  private val currentYear = LocalDate.now
  private val nextYear = currentYear.plusYears(1)

  object domain {
    val messageCreation = CreateMessage(
      sender = "Dave",
      content = "Open the pod bay doors, HAL."
    )

    val messageUpdate = UpdateMessage(
      sender = Some("HAL"),
      content = Some("Sorry Dave. I'm afraid I cannot do that.")
    )

    val message = Message(
      uuid = UUID.randomUUID(),
      sender = "Dave",
      content = "Open the pod bay doors, HAL."
    )
  }

  object rest {
    val messageCreation = MessageCreation(
      sender = domain.messageCreation.sender,
      content = domain.messageCreation.content
    )

    val messageUpdate = MessageUpdate(
      sender = domain.messageUpdate.sender,
      content = domain.messageUpdate.content
    )

    val message = MessageView(
      uuid = domain.message.uuid,
      sender = domain.message.sender,
      content = domain.message.content
    )
  }
}
