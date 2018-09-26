package com.ferris.timetable.sample

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.util.UUID

import com.ferris.planning.contract.resource.Resources.Out.{ PortionView, ThreadView, WeaveView, HobbyView }
import com.ferris.timetable.contract.resource.Resources.In._
import com.ferris.timetable.contract.resource.Resources.Out._
import com.ferris.timetable.model.Model._
import com.ferris.timetable.command.Commands._
import com.ferris.utils.DefaultTimerComponent

object SampleData extends DefaultTimerComponent {

  private val now = LocalTime.now.truncatedTo(ChronoUnit.MINUTES)
  private val later = now.plusHours(1L)
  private val today = LocalDate.now

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
      uuid = UUID.randomUUID,
      sender = "Dave",
      content = "Open the pod bay doors, HAL."
    )

    val taskTemplateCreation = CreateTaskTemplate(
      taskId = None,
      `type` = TaskTypes.LaserDonut
    )

    val taskTemplate = TaskTemplate(
      taskId = None,
      `type` = TaskTypes.LaserDonut
    )

    val timeBlockTemplateCreation = CreateTimeBlockTemplate(
      start = now,
      finish = later,
      task = taskTemplateCreation
    )

    val timeBlockTemplate = TimeBlockTemplate(
      start = now,
      finish = later,
      task = taskTemplate
    )

    val timetableTemplateCreation = CreateTimetableTemplate(
      blocks = timeBlockTemplateCreation :: Nil
    )

    val timetableTemplate = TimetableTemplate(
      blocks = timeBlockTemplate :: Nil
    )

    val routineCreation = CreateRoutine(
      name = "Autumn",
      monday = timetableTemplateCreation,
      tuesday = timetableTemplateCreation,
      wednesday = timetableTemplateCreation,
      thursday = timetableTemplateCreation,
      friday = timetableTemplateCreation,
      saturday = timetableTemplateCreation,
      sunday = timetableTemplateCreation
    )

    val routineUpdate = UpdateRoutine(
      name = Some("Winter"),
      monday = Some(timetableTemplateCreation),
      tuesday = Some(timetableTemplateCreation),
      wednesday = Some(timetableTemplateCreation),
      thursday = Some(timetableTemplateCreation),
      friday = Some(timetableTemplateCreation),
      saturday = Some(timetableTemplateCreation),
      sunday = Some(timetableTemplateCreation)
    )

    val routine = Routine(
      uuid = UUID.randomUUID,
      name = "Autumn",
      monday = timetableTemplate,
      tuesday = timetableTemplate,
      wednesday = timetableTemplate,
      thursday = timetableTemplate,
      friday = timetableTemplate,
      saturday = timetableTemplate,
      sunday = timetableTemplate,
      isCurrent = true
    )

    val scheduledTaskCreation = CreateScheduledTask(
      taskId = UUID.randomUUID,
      `type` = TaskTypes.LaserDonut
    )

    val scheduledTask = ScheduledTask(
      taskId = UUID.randomUUID,
      `type` = TaskTypes.LaserDonut,
      isDone = false
    )

    val scheduledTimeBlockCreation = CreateScheduledTimeBlock(
      start = now,
      finish = later,
      task = scheduledTaskCreation
    )

    val scheduledTimeBlockUpdate = UpdateScheduledTimeBlock(
      start = now,
      finish = later,
      done = true
    )

    val concreteBlock = ConcreteBlock(
      start = now,
      finish = later,
      task = scheduledTask
    )

    val bufferBlock = BufferBlock(
      start = now,
      finish = later,
      firstTask = scheduledTask,
      secondTask = scheduledTask
    )

    val timetableCreation = CreateTimetable(
      date = today,
      blocks = scheduledTimeBlockCreation :: Nil
    )

    val timetableUpdate = UpdateTimetable(
      blocks = scheduledTimeBlockUpdate :: Nil
    )

    val timetable = Timetable(
      date = today,
      blocks = concreteBlock :: bufferBlock :: concreteBlock :: Nil
    )
  }

  object rest {
    import com.ferris.timetable.service.conversions.TypeResolvers._

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

    val taskTemplateCreation = TaskTemplateCreation(
      taskId = domain.taskTemplateCreation.taskId,
      `type` = TaskType.toString(domain.taskTemplateCreation.`type`)
    )

    val taskTemplate = TaskTemplateView(
      taskId = domain.taskTemplate.taskId,
      `type` = TaskType.toString(domain.taskTemplate.`type`),
      summary = Some("Do stuff")
    )

    val timeBlockTemplateCreation = TimeBlockTemplateCreation(
      start = domain.timeBlockTemplateCreation.start,
      finish = domain.timeBlockTemplateCreation.finish,
      task = taskTemplateCreation
    )

    val timeBlockTemplate = TimeBlockTemplateView(
      start = domain.timeBlockTemplate.start,
      finish = domain.timeBlockTemplate.finish,
      task = taskTemplate
    )

    val timetableTemplateCreation = TimetableTemplateCreation(
      blocks = timeBlockTemplateCreation :: Nil
    )

    val timetableTemplate = TimetableTemplateView(
      blocks = timeBlockTemplate :: Nil
    )

    val routineCreation = RoutineCreation(
      name = domain.routineCreation.name,
      monday = timetableTemplateCreation,
      tuesday = timetableTemplateCreation,
      wednesday = timetableTemplateCreation,
      thursday = timetableTemplateCreation,
      friday = timetableTemplateCreation,
      saturday = timetableTemplateCreation,
      sunday = timetableTemplateCreation
    )

    val routineUpdate = RoutineUpdate(
      name = domain.routineUpdate.name,
      monday = Some(timetableTemplateCreation),
      tuesday = Some(timetableTemplateCreation),
      wednesday = Some(timetableTemplateCreation),
      thursday = Some(timetableTemplateCreation),
      friday = Some(timetableTemplateCreation),
      saturday = Some(timetableTemplateCreation),
      sunday = Some(timetableTemplateCreation)
    )

    val routine = RoutineView(
      uuid = domain.routine.uuid,
      name = domain.routine.name,
      monday = timetableTemplate,
      tuesday = timetableTemplate,
      wednesday = timetableTemplate,
      thursday = timetableTemplate,
      friday = timetableTemplate,
      saturday = timetableTemplate,
      sunday = timetableTemplate
    )

    val scheduledTask = ScheduledTaskView(
      taskId = domain.scheduledTask.taskId,
      `type` = TaskType.toString(domain.scheduledTask.`type`),
      summary = Some("Do stuff now!"),
      isDone = true
    )

    val scheduledTimeBlockUpdate = ScheduledTimeBlockUpdate(
      start = domain.scheduledTimeBlockCreation.start,
      finish = domain.scheduledTimeBlockCreation.finish,
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
}
