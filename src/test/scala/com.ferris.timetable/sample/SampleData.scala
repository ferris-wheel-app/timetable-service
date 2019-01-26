package com.ferris.timetable.sample

import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.util.UUID

import com.ferris.planning.contract.resource.Resources.In.AssociatedSkillInsertion
import com.ferris.planning.contract.resource.Resources.Out._
import com.ferris.timetable.contract.resource.Resources.In._
import com.ferris.timetable.contract.resource.Resources.Out._
import com.ferris.timetable.model.Model._
import com.ferris.timetable.command.Commands._
import com.ferris.utils.DefaultTimerComponent

object SampleData extends DefaultTimerComponent {

  private val now = LocalTime.of(6, 0)
  private val later = now.plusHours(16L)
  private val today = LocalDate.now

  object domain {
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
      done = false
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

    val taskTemplateCreation = TaskTemplateCreation(
      taskId = domain.taskTemplateCreation.taskId,
      `type` = TaskType.toString(domain.taskTemplateCreation.`type`)
    )

    val taskTemplate = TaskTemplateView(
      taskId = domain.taskTemplate.taskId,
      `type` = TaskType.toString(domain.taskTemplate.`type`),
      summary = None
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
      summary = None,
      isDone = false
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

    val skillCategory = SkillCategoryView(
      uuid = UUID.randomUUID,
      name = "Functional Programming",
      categoryId = UUID.randomUUID
    )

    val skill = SkillView(
      uuid = UUID.randomUUID,
      name = "Cats",
      categoryId = UUID.randomUUID,
      proficiency = "intermediate",
      practisedHours = 500L,
      lastApplied = Some(LocalDateTime.now)
    )

    val associatedSkillInsertion = AssociatedSkillInsertion(
      skillId = UUID.randomUUID,
      relevance = "maintenance",
      level = "intermediate"
    )

    val associatedSkill = AssociatedSkillView(
      skillId = UUID.randomUUID,
      relevance = "maintenance",
      level = "intermediate"
    )

    val thread = ThreadView(
      uuid = UUID.randomUUID,
      goalId = Some(UUID.randomUUID),
      summary = "Go for a run",
      description = "Go for a run",
      associatedSkills = associatedSkill :: Nil,
      performance = "on-track",
      createdOn = LocalDateTime.now,
      lastModified = None,
      lastPerformed = None
    )

    val weave = WeaveView(
      uuid = UUID.randomUUID,
      goalId = Some(UUID.randomUUID),
      summary = "Organise a tech lecture",
      description = "Create a presentation about Kafka",
      associatedSkills = associatedSkill :: Nil,
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
      associatedSkills = associatedSkill :: Nil,
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
      associatedSkills = associatedSkill :: Nil,
      frequency = "continuous",
      `type` = "active",
      createdOn = LocalDateTime.now,
      lastModified = None,
      lastPerformed = None
    )

    val oneOff = OneOffView(
      uuid = UUID.randomUUID,
      goalId = Some(UUID.randomUUID),
      description = "Get window fixed",
      associatedSkills = associatedSkill :: Nil,
      estimate = 14400000L,
      order = 5,
      status = "planned",
      createdOn = LocalDateTime.now,
      lastModified = Some(LocalDateTime.now),
      lastPerformed = Some(LocalDateTime.now)
    )

    val scheduledOneOff = ScheduledOneOffView(
      occursOn = LocalDateTime.now,
      uuid = UUID.randomUUID,
      goalId = Some(UUID.randomUUID),
      description = "Get window fixed",
      associatedSkills = associatedSkill :: Nil,
      estimate = 14400000L,
      status = "planned",
      createdOn = LocalDateTime.now,
      lastModified = Some(LocalDateTime.now),
      lastPerformed = Some(LocalDateTime.now)
    )
  }
}
