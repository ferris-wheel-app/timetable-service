package com.ferris.timetable.contract.sample

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.util.UUID

import com.ferris.planning.contract.resource.Resources.Out.{HobbyView, PortionView, ThreadView, WeaveView}
import com.ferris.timetable.contract.resource.Resources.In._
import com.ferris.timetable.contract.resource.Resources.Out._

object SampleData {

  private val now = LocalTime.now.truncatedTo(ChronoUnit.MINUTES)
  private val later = now.plusHours(1L)
  private val today = LocalDate.now

  val messageCreation = MessageCreation(
    sender = "Dave",
    content = "Open the pod bay doors, HAL."
  )

  val messageUpdate = MessageUpdate(
    sender = Some("HAL"),
    content = Some("Sorry Dave. I'm afraid I cannot do that.")
  )

  val message = MessageView(
    uuid = UUID.randomUUID,
    sender = "Dave",
    content = "Open the pod bay doors, HAL."
  )

  val taskTemplateCreation = TaskTemplateCreation(
    taskId = None,
    `type` = "laser_donut"
  )

  val taskTemplate = TaskTemplateView(
    taskId = None,
    `type` = "laser_donut",
    summary = Some("Bake-off training")
  )

  val timeBlockTemplateCreation = TimeBlockTemplateCreation(
    start = now,
    finish = later,
    task = taskTemplateCreation
  )

  val timeBlockTemplate = TimeBlockTemplateView(
    start = now,
    finish = later,
    task = taskTemplate
  )

  val timetableTemplateCreation = TimetableTemplateCreation(
    blocks = timeBlockTemplateCreation :: Nil
  )

  val timetableTemplate = TimetableTemplateView(
    blocks = timeBlockTemplate :: Nil
  )

  val routineCreation = RoutineCreation(
    name = "Autumn",
    monday = timetableTemplateCreation,
    tuesday = timetableTemplateCreation,
    wednesday = timetableTemplateCreation,
    thursday = timetableTemplateCreation,
    friday = timetableTemplateCreation,
    saturday = timetableTemplateCreation,
    sunday = timetableTemplateCreation
  )

  val routineUpdate = RoutineUpdate(
    name = Some("Winter"),
    monday = Some(timetableTemplateCreation),
    tuesday = Some(timetableTemplateCreation),
    wednesday = Some(timetableTemplateCreation),
    thursday = Some(timetableTemplateCreation),
    friday = Some(timetableTemplateCreation),
    saturday = Some(timetableTemplateCreation),
    sunday = Some(timetableTemplateCreation)
  )

  val routine = RoutineView(
    uuid = UUID.randomUUID,
    name = "Autumn",
    monday = timetableTemplate,
    tuesday = timetableTemplate,
    wednesday = timetableTemplate,
    thursday = timetableTemplate,
    friday = timetableTemplate,
    saturday = timetableTemplate,
    sunday = timetableTemplate
  )

  val scheduledTask = ScheduledTaskView(
    taskId = UUID.randomUUID,
    `type` = "laser_donut",
    summary = None,
    isDone = false
  )

  val scheduledTimeBlockUpdate = ScheduledTimeBlockUpdate(
    start = now,
    finish = later,
    done = true
  )

  val concreteBlock = ConcreteBlockView(
    start = now,
    finish = later,
    task = scheduledTask
  )

  val bufferBlock = BufferBlockView(
    start = now,
    finish = later,
    firstTask = scheduledTask,
    secondTask = scheduledTask
  )

  val timetableUpdate = TimetableUpdate(
    blocks = scheduledTimeBlockUpdate :: Nil
  )

  val timetable = TimetableView(
    date = today,
    blocks = concreteBlock :: bufferBlock :: concreteBlock :: Nil
  )

  val thread = ThreadView(
    uuid = UUID.randomUUID,
    goalId = Some(UUID.randomUUID),
    summary = "Go for a run",
    description = "Go for a run",
    status = "planned",
    createdOn = LocalDateTime.now,
    lastModified = None,
    lastPerformed = None
  )

  val weave = WeaveView(
    uuid = UUID.randomUUID,
    goalId = Some(UUID.randomUUID),
    summary = "Organise a tech lecture",
    description = "Create a presentation about Kafka",
    `type` = "PDR",
    status = "planned",
    createdOn = LocalDateTime.now,
    lastModified = None,
    lastPerformed = None
  )

  val portion = PortionView(
    uuid = UUID.randomUUID,
    laserDonutId = UUID.randomUUID,
    summary = "Write tests for the TimetableService.",
    order = 1,
    status = "planned",
    createdOn = LocalDateTime.now,
    lastModified = None,
    lastPerformed = None
  )

  val hobby = HobbyView(
    uuid = UUID.randomUUID,
    goalId = Some(UUID.randomUUID),
    summary = "Yoga",
    description = "Train in Acro-Yoga",
    frequency = "continuous",
    `type` = "active",
    status = "planned",
    createdOn = LocalDateTime.now,
    lastModified = None,
    lastPerformed = None
  )
}
