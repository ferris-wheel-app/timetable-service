package com.ferris.timetable.contract.resource

import java.util.UUID

object Resources {

  object In {

    case class MessageCreation (
      sender: String,
      content: String
    )

    case class MessageUpdate (
      sender: Option[String],
      content: Option[String]
    )
  }

  object Out {

    case class MessageView (
      uuid: UUID,
      sender: String,
      content: String
    )

    case class DeletionResult(
      isSuccessful: Boolean
    )

    object DeletionResult {
      val successful = DeletionResult(true)
      val unsuccessful = DeletionResult(false)
    }
  }
}
