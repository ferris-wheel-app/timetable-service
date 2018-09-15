package com.ferris.timetable.contract.validation

import java.time.Duration

import com.ferris.microservice.validation.InputValidation
import com.ferris.timetable.contract.resource.Resources.In._
import com.ferris.timetable.contract.resource.TypeFields.TaskType

object InputValidators extends InputValidation {

  private val TypeField = "type"
  private val BlocksField = "blocks"
  private val StartField = "start"

  private val WholeDay = 86400L

  // Check that the time-blocks are in order.
  // Check that there are no gaps and overlaps between time-blocks.
  // Check that the total duration of the combined time-blocks is exactly 24 hours.
  // Check that time-blocks are provided.
  def checkValidity(creation: TimetableTemplateCreation): Unit = {
    checkField(blocksAreChained(creation.blocks), BlocksField, "Time-blocks should be provided in order and not contain any gaps or overlaps")
    checkField(occupiesWholeDay(creation.blocks), BlocksField, "Time-blocks should add up to exactly 24 hours")
  }

  // Check that the start-time occurs before the finish-time.
  def checkValidity(creation: TimeBlockTemplateCreation): Unit = {
    checkField(creation.start.isBefore(creation.finish), StartField, "The start-time should occur before the finish-time")
  }

  // Check that the task-type is valid.
  def checkValidity(creation: TaskTemplateCreation): Unit = {
    checkField(TaskType.values.contains(creation.`type`), TypeField)
  }

  private def blocksAreChained(blocks: Seq[TimeBlockTemplateCreation]): Boolean = {
    slidingPairs(blocks).foldLeft(true) { case (soFar, (previous, next)) =>
      soFar && (previous.finish == next.start)
    }
  }

  private def occupiesWholeDay(blocks: Seq[TimeBlockTemplateCreation]): Boolean = {
    blocks.foldLeft(0L) { case (occupiedPeriod, block) =>
      occupiedPeriod + Duration.between(block.start, block.finish).getSeconds
    } == WholeDay
  }

  private def slidingPairs[T](list: Seq[T]): Seq[(T, T)] = {
    list.sliding(2, 1).collect { case Seq(a, b) => (a, b) }.toSeq
  }
}
