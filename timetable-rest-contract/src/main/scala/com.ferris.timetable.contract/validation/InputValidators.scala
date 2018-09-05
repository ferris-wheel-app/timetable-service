package com.ferris.timetable.contract.validation

import com.ferris.microservice.validation.InputValidation
import com.ferris.timetable.contract.resource.Resources.In._

object InputValidators extends InputValidation {

  private val TypeField = "type"

  // Check that the time-blocks are in order.
  // Check that there are no gaps and overlaps between time-blocks.
  // Check that the total duration of the combined time-blocks is exactly 24 hours.
  // Check that time-blocks are provided.
  def checkValidity(creation: TimetableTemplateCreation): Unit = {
    ???
  }

  // Check that the start-time occurs before the finish-time.
  def checkValidity(creation: TimeBlockTemplateCreation): Unit = {
    ???
  }

  // Check that the task-type is valid.
  def checkValidity(creation: TaskTemplateCreation): Unit = {
    ???
  }

  private def blocksAreChained(creation: TimetableTemplateCreation): Boolean = {
    val slidingPairs = creation.blocks.sliding(2, 1).collect { case Seq(a, b) => (a, b) }
    slidingPairs.foldLeft(true) { case (soFar, (previous, next)) =>
      soFar && (previous.finish == next.start)
    }
  }
}
