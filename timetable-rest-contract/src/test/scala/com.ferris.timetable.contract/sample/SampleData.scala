package com.ferris.timetable.contract.sample

import java.util.UUID

import com.ferris.timetable.contract.resource.Resources.In._
import com.ferris.timetable.contract.resource.Resources.Out._

object SampleData {

  val messageCreation = MessageCreation(
    sender = "Dave",
    content = "Open the pod bay doors, HAL."
  )

  val messageUpdate = MessageUpdate(
    sender = Some("HAL"),
    content = Some("Sorry Dave. I'm afraid I cannot do that.")
  )

  val message = MessageView(
    uuid = UUID.randomUUID(),
    sender = "Dave",
    content = "Open the pod bay doors, HAL."
  )
}
