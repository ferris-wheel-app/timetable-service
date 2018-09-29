package com.ferris.timetable.contract.validation

import java.time.Duration

import com.ferris.microservice.validation.InputValidation
import com.ferris.timetable.contract.resource.Resources.In._
import com.ferris.timetable.contract.resource.TypeFields.TaskType

object InputValidators extends InputValidation {

  private val TypeField = "type"
  private val BlocksField = "blocks"
  private val StartField = "start"

  private val MinimumActiveHours = 43200L
  private val MaximumActiveHours = 57600L

  // Check that the time-blocks are in order.
  // Check that there are no gaps and overlaps between time-blocks.
  // Check that the total duration of the combined time-blocks is exactly 24 hours.
  // Check that time-blocks are provided.
  def checkValidity(creation: TimetableTemplateCreation): Unit = {
    checkField(blocksAreChained(creation.blocks), BlocksField, "Time-blocks should be provided in order and not contain any gaps or overlaps")
    checkField(occupiesAcceptableDuration(creation.blocks), BlocksField, "Time-blocks should in total be at least 12 hours and no more than 16 hours")
  }

  // Check that the start-time occurs before the finish-time.
  def checkValidity(creation: TimeBlockTemplateCreation): Unit = {
    checkField(creation.start.isBefore(creation.finish), StartField, "The start-time should occur before the finish-time")
  }

  // Check that the task-type is valid.
  def checkValidity(creation: TaskTemplateCreation): Unit = {
    checkField(TaskType.values.contains(creation.`type`), TypeField)
  }

  def checkValidity(update: TimetableUpdate): Unit = {
    checkField(update.blocks.forall(block => block.start.isBefore(block.finish)), BlocksField, "Invalid time-blocks have been provided")
  }

  private def blocksAreChained(blocks: Seq[TimeBlockTemplateCreation]): Boolean = {
    slidingPairs(blocks).foldLeft(true) { case (soFar, (previous, next)) =>
      soFar && (previous.finish == next.start)
    }
  }

  private def occupiesAcceptableDuration(blocks: Seq[TimeBlockTemplateCreation]): Boolean = {
    val totalDuration = blocks.foldLeft(0L) { case (occupiedPeriod, block) =>
      occupiedPeriod + Duration.between(block.start, block.finish).getSeconds
    }
    totalDuration >= MinimumActiveHours && totalDuration <= MaximumActiveHours
  }

  private def slidingPairs[T](list: Seq[T]): Seq[(T, T)] = {
    list.sliding(2, 1).collect { case Seq(a, b) => (a, b) }.toSeq
  }
}
