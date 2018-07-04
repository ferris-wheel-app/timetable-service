package com.ferris.timetable.sample

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import com.ferris.planning.command.Commands._
import com.ferris.planning.contract.resource.Resources.In._
import com.ferris.planning.contract.resource.Resources.Out._
import com.ferris.planning.model.Model._
import com.ferris.planning.service.conversions.TypeResolvers._

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

    val backlogItemCreation = CreateBacklogItem(
      summary = "I need to get my shit together",
      description = "I need to get my shit together",
      `type` = BacklogItemTypes.Issue
    )

    val backlogItemUpdate = UpdateBacklogItem(
      summary = Some("I want to be the best of the best at programming"),
      description = Some("I want to be the best of the best at programming"),
      `type` = Some(BacklogItemTypes.Idea)
    )

    val backlogItem = BacklogItem(
      uuid = UUID.randomUUID,
      summary = "I need to get my shit together",
      description = "I need to get my shit together",
      `type` = BacklogItemTypes.Issue,
      createdOn = LocalDateTime.now,
      lastModified = Some(LocalDateTime.now)
    )

    val epochCreation = CreateEpoch(
      name = "Messinaissance",
      totem = "Hero",
      question = "Am I capable of becoming an Übermensch?"
    )

    val epochUpdate = UpdateEpoch(
      name = Some("Wakanda"),
      totem = Some("Leader"),
      question = Some("Is Africa capable of achieving full development?")
    )

    val epoch = Epoch(
      uuid = UUID.randomUUID,
      name = "Messinaissance",
      totem = "Hero",
      question = "Am I capable of becoming an Übermensch?",
      createdOn = LocalDateTime.now,
      lastModified = Some(LocalDateTime.now)
    )

    val yearCreation = CreateYear(
      epochId = UUID.randomUUID,
      startDate = currentYear
    )

    val yearUpdate = UpdateYear(
      epochId = Some(UUID.randomUUID),
      startDate = Some(currentYear.plusYears(2))
    )

    val year = Year(
      uuid = UUID.randomUUID,
      epochId = UUID.randomUUID,
      startDate = currentYear,
      finishDate = nextYear,
      createdOn = LocalDateTime.now,
      lastModified = Some(LocalDateTime.now)
    )

    val themeCreation = CreateTheme(
      yearId = UUID.randomUUID,
      name = "Career Capital"
    )

    val themeUpdate = UpdateTheme(
      yearId = Some(UUID.randomUUID),
      name = Some("Mission")
    )

    val theme = Theme(
      uuid = UUID.randomUUID,
      yearId = UUID.randomUUID,
      name = "Career Capital",
      createdOn = LocalDateTime.now,
      lastModified = Some(LocalDateTime.now)
    )

    val goalCreation = CreateGoal(
      themeId = UUID.randomUUID,
      backlogItems = Nil,
      summary = "Master at least one foreign language",
      description = "Learn French, Italian, and Korean",
      level = 1,
      priority = false,
      graduation = GraduationTypes.Hobby,
      status = GoalStatuses.NotAchieved
    )

    val goalUpdate = UpdateGoal(
      themeId = Some(UUID.randomUUID),
      backlogItems = Some(Nil),
      summary = Some("Learn to play an instrument"),
      description = Some("Learn to play the piano, the guitar, and the saxophone"),
      level = Some(2),
      priority = Some(false),
      graduation = Some(GraduationTypes.Abandoned),
      status = Some(GoalStatuses.Employed)
    )

    val goal = Goal(
      uuid = UUID.randomUUID,
      themeId = UUID.randomUUID,
      backlogItems = UUID.randomUUID :: UUID.randomUUID :: Nil,
      summary = "Master at least one foreign language",
      description = "Learn French, Italian, and Korean",
      level = 1,
      priority = false,
      graduation = GraduationTypes.Hobby,
      status = GoalStatuses.NotAchieved,
      createdOn = LocalDateTime.now,
      lastModified = Some(LocalDateTime.now)
    )

    val threadCreation = CreateThread(
      goalId = Some(UUID.randomUUID),
      summary = "Go for a run",
      description = "Go for a run",
      status = Statuses.Planned
    )

    val threadUpdate = UpdateThread(
      goalId = Some(UUID.randomUUID),
      summary = Some("Sleep"),
      description = Some("Sleep for 8 hours"),
      status = Some(Statuses.InProgress)
    )

    val thread = Thread(
      uuid = UUID.randomUUID,
      goalId = Some(UUID.randomUUID),
      summary = "Go for a run",
      description = "Go for a run",
      status = Statuses.Planned,
      createdOn = LocalDateTime.now,
      lastModified = Some(LocalDateTime.now),
      lastPerformed = Some(LocalDateTime.now)
    )

    val weaveCreation = CreateWeave(
      goalId = Some(UUID.randomUUID),
      summary = "Organise a tech lecture",
      description = "Create a presentation about Kafka",
      `type` = WeaveTypes.PDR,
      status = Statuses.Planned
    )

    val weaveUpdate = UpdateWeave(
      goalId = Some(UUID.randomUUID),
      summary = Some("Apply your new-found Go knowledge"),
      description = Some("Create a snuffleupagus"),
      `type` = Some(WeaveTypes.BAU),
      status = Some(Statuses.Complete)
    )

    val weave = Weave(
      uuid = UUID.randomUUID,
      goalId = Some(UUID.randomUUID),
      summary = "Organise a tech lecture",
      description = "Create a presentation about Kafka",
      `type` = WeaveTypes.PDR,
      status = Statuses.Planned,
      createdOn = LocalDateTime.now,
      lastModified = Some(LocalDateTime.now),
      lastPerformed = Some(LocalDateTime.now)
    )

    val laserDonutCreation = CreateLaserDonut(
      goalId = UUID.randomUUID,
      summary = "Implement initial microservices",
      description = "Implement planning-service, timetable-service, and history-service, in a microservices-based architecture",
      milestone = "A deployed backend service",
      `type` = DonutTypes.ProjectFocused,
      status = Statuses.InProgress
    )

    val laserDonutUpdate = UpdateLaserDonut(
      goalId = Some(UUID.randomUUID),
      summary = Some("Create the front-end"),
      description = Some("Use React"),
      milestone = Some("A basic working prototype"),
      `type` = Some(DonutTypes.ProjectFocused),
      status = Some(Statuses.InProgress)
    )

    val laserDonut = LaserDonut(
      uuid = UUID.randomUUID,
      goalId = UUID.randomUUID,
      summary = "Implement initial microservices",
      description = "Implement planning-service, timetable-service, and history-service, in a microservices-based architecture",
      milestone = "A basic working prototype",
      order = 1,
      `type` = DonutTypes.SkillFocused,
      status = Statuses.Planned,
      createdOn = LocalDateTime.now,
      lastModified = Some(LocalDateTime.now),
      lastPerformed = Some(LocalDateTime.now)
    )

    val portionCreation = CreatePortion(
      laserDonutId = UUID.randomUUID,
      summary = "Write tests",
      status = Statuses.InProgress
    )

    val portionUpdate = UpdatePortion(
      laserDonutId = Some(UUID.randomUUID),
      summary = Some("Split into sub-projects"),
      status = Some(Statuses.InProgress)
    )

    val portion = Portion(
      uuid = UUID.randomUUID,
      laserDonutId = UUID.randomUUID,
      summary = "Write tests",
      order = 13,
      status = Statuses.InProgress,
      createdOn = LocalDateTime.now,
      lastModified = Some(LocalDateTime.now),
      lastPerformed = Some(LocalDateTime.now)
    )

    val todoCreation = CreateTodo(
      portionId = UUID.randomUUID,
      description = "Create sample data for tests",
      status = Statuses.Complete
    )

    val todoUpdate = UpdateTodo(
      portionId = Some(UUID.randomUUID),
      description = Some("Create repository tests"),
      status = Some(Statuses.Complete)
    )

    val todo = Todo(
      uuid = UUID.randomUUID,
      portionId = UUID.randomUUID,
      description = "Create sample data for tests",
      order = 4,
      status = Statuses.Complete,
      createdOn = LocalDateTime.now,
      lastModified = Some(LocalDateTime.now),
      lastPerformed = Some(LocalDateTime.now)
    )

    val hobbyCreation = CreateHobby(
      goalId = Some(UUID.randomUUID),
      summary = "Yoga",
      description = "Train in Acro-Yoga",
      frequency = HobbyFrequencies.Continuous,
      `type` = HobbyTypes.Active,
      status = Statuses.Planned
    )

    val hobbyUpdate = UpdateHobby(
      goalId = Some(UUID.randomUUID),
      summary = Some("Play ping-pong"),
      description = Some("Table tennis"),
      frequency = Some(HobbyFrequencies.Continuous),
      `type` = Some(HobbyTypes.Active),
      status = Some(Statuses.Planned)
    )

    val hobby = Hobby(
      uuid = UUID.randomUUID,
      goalId = Some(UUID.randomUUID),
      summary = "Yoga",
      description = "Train in Acro-Yoga",
      frequency = HobbyFrequencies.Continuous,
      `type` = HobbyTypes.Active,
      status = Statuses.Planned,
      createdOn = LocalDateTime.now,
      lastModified = Some(LocalDateTime.now),
      lastPerformed = Some(LocalDateTime.now)
    )

    val listUpdate = UpdateList(
      reordered = UUID.randomUUID :: UUID.randomUUID :: Nil
    )

    val tierUpsert = UpsertTier(
      laserDonuts = (1 to 5).map(_ => UUID.randomUUID)
    )

    val tier = Tier(
      laserDonuts = UUID.randomUUID :: UUID.randomUUID :: Nil
    )

    val pyramidUpsert = UpsertPyramidOfImportance(
      tiers = (1 to 5).map(_ => tierUpsert)
    )

    val pyramid = PyramidOfImportance(
      tiers = tier :: Nil,
      currentLaserDonut = Some(UUID.randomUUID)
    )

    val scheduledTodo = ScheduledTodo(
      uuid = UUID.randomUUID,
      order = 1,
      status = Statuses.Planned
    )

    val scheduledPortion = ScheduledPortion(
      id = 1,
      uuid = UUID.randomUUID,
      todos = scheduledTodo :: Nil,
      order = 1,
      status = Statuses.Planned
    )

    val scheduledLaserDonut = ScheduledLaserDonut(
      id = 1,
      uuid = UUID.randomUUID,
      portions = scheduledPortion :: Nil,
      tier = 1,
      status = Statuses.Planned,
      lastPerformed = Some(LocalDateTime.now)
    )

    val scheduledPyramid = ScheduledPyramid(
      laserDonuts = scheduledLaserDonut :: Nil,
      currentLaserDonut = Some(2),
      currentPortion = Some(3),
      lastUpdate = Some(LocalDateTime.now)
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

    val backlogItemCreation = BacklogItemCreation(
      summary = domain.backlogItemCreation.summary,
      description = domain.backlogItemCreation.description,
      `type` = BacklogItemType.toString(domain.backlogItemCreation.`type`)
    )

    val backlogItemUpdate = BacklogItemUpdate(
      summary = domain.backlogItemUpdate.summary,
      description = domain.backlogItemUpdate.description,
      `type` = domain.backlogItemUpdate.`type`.map(BacklogItemType.toString)
    )

    val backlogItem = BacklogItemView(
      uuid = domain.backlogItem.uuid,
      summary = domain.backlogItem.summary,
      description = domain.backlogItem.description,
      `type` = BacklogItemType.toString(domain.backlogItem.`type`),
      createdOn = domain.backlogItem.createdOn,
      lastModified = domain.backlogItem.lastModified
    )

    val epochCreation = EpochCreation(
      name = domain.epochCreation.name,
      totem = domain.epochCreation.totem,
      question = domain.epochCreation.question
    )

    val epochUpdate = EpochUpdate(
      name = domain.epochUpdate.name,
      totem = domain.epochUpdate.totem,
      question = domain.epochUpdate.question
    )

    val epoch = EpochView(
      uuid = domain.epoch.uuid,
      name = domain.epoch.name,
      totem = domain.epoch.totem,
      question = domain.epoch.question,
      createdOn = domain.epoch.createdOn,
      lastModified = domain.epoch.lastModified
    )

    val yearCreation = YearCreation(
      epochId = domain.yearCreation.epochId,
      startDate = domain.yearCreation.startDate
    )

    val yearUpdate = YearUpdate(
      epochId = domain.yearUpdate.epochId,
      startDate = domain.yearUpdate.startDate
    )

    val year = YearView(
      uuid = domain.year.uuid,
      epochId = domain.year.epochId,
      startDate = domain.year.startDate,
      finishDate = domain.year.finishDate,
      createdOn = domain.year.createdOn,
      lastModified = domain.year.lastModified
    )

    val themeCreation = ThemeCreation(
      yearId = domain.themeCreation.yearId,
      name = domain.themeCreation.name
    )

    val themeUpdate = ThemeUpdate(
      yearId = domain.themeUpdate.yearId,
      name = domain.themeUpdate.name
    )

    val theme = ThemeView(
      uuid = domain.theme.uuid,
      yearId = domain.theme.yearId,
      name = domain.theme.name,
      createdOn = domain.theme.createdOn,
      lastModified = domain.theme.lastModified
    )

    val goalCreation = GoalCreation(
      themeId = domain.goalCreation.themeId,
      backlogItems = domain.goalCreation.backlogItems,
      summary = domain.goalCreation.summary,
      description = domain.goalCreation.description,
      level = domain.goalCreation.level,
      priority = domain.goalCreation.priority,
      graduation = GraduationType.toString(domain.goalCreation.graduation),
      status = GoalStatus.toString(domain.goalCreation.status)
    )

    val goalUpdate = GoalUpdate(
      themeId = domain.goalUpdate.themeId,
      backlogItems = domain.goalUpdate.backlogItems,
      summary = domain.goalUpdate.summary,
      description = domain.goalUpdate.description,
      level = domain.goalUpdate.level,
      priority = domain.goalUpdate.priority,
      graduation = domain.goalUpdate.graduation.map(GraduationType.toString),
      status = domain.goalUpdate.status.map(GoalStatus.toString)
    )

    val goal = GoalView(
      uuid = domain.goal.uuid,
      themeId = domain.goal.themeId,
      backlogItems = domain.goal.backlogItems,
      summary = domain.goal.summary,
      description = domain.goal.description,
      level = domain.goal.level,
      priority = domain.goal.priority,
      graduation = GraduationType.toString(domain.goal.graduation),
      status = GoalStatus.toString(domain.goal.status),
      createdOn = domain.goal.createdOn,
      lastModified = domain.goal.lastModified
    )

    val threadCreation = ThreadCreation(
      goalId = domain.threadCreation.goalId,
      summary = domain.threadCreation.summary,
      description = domain.threadCreation.description,
      status = Status.toString(domain.threadCreation.status)
    )

    val threadUpdate = ThreadUpdate(
      goalId = domain.threadUpdate.goalId,
      summary = domain.threadUpdate.summary,
      description = domain.threadUpdate.description,
      status = domain.threadUpdate.status.map(Status.toString)
    )

    val thread = ThreadView(
      uuid = domain.thread.uuid,
      goalId = domain.thread.goalId,
      summary = domain.thread.summary,
      description = domain.thread.description,
      status = Status.toString(domain.thread.status),
      createdOn = domain.thread.createdOn,
      lastModified = domain.thread.lastModified,
      lastPerformed = domain.thread.lastPerformed
    )

    val weaveCreation = WeaveCreation(
      goalId = domain.weaveCreation.goalId,
      summary = domain.weaveCreation.summary,
      description = domain.weaveCreation.description,
      `type` = WeaveType.toString(domain.weaveCreation.`type`),
      status = Status.toString(domain.weaveCreation.status)
    )

    val weaveUpdate = WeaveUpdate(
      goalId = domain.weaveUpdate.goalId,
      summary = domain.weaveUpdate.summary,
      description = domain.weaveUpdate.description,
      `type` = domain.weaveUpdate.`type`.map(WeaveType.toString),
      status = domain.weaveUpdate.status.map(Status.toString)
    )

    val weave = WeaveView(
      uuid = domain.weave.uuid,
      goalId = domain.weave.goalId,
      summary = domain.weave.summary,
      description = domain.weave.description,
      `type` = WeaveType.toString(domain.weave.`type`),
      status = Status.toString(domain.weave.status),
      createdOn = domain.weave.createdOn,
      lastModified = domain.weave.lastModified,
      lastPerformed = domain.weave.lastPerformed
    )

    val laserDonutCreation = LaserDonutCreation(
      goalId = domain.laserDonutCreation.goalId,
      summary = domain.laserDonutCreation.summary,
      description = domain.laserDonutCreation.description,
      milestone = domain.laserDonutCreation.milestone,
      `type` = DonutType.toString(domain.laserDonutCreation.`type`),
      status = Status.toString(domain.laserDonutCreation.status)
    )

    val laserDonutUpdate = LaserDonutUpdate(
      goalId = domain.laserDonutUpdate.goalId,
      summary = domain.laserDonutUpdate.summary,
      description = domain.laserDonutUpdate.description,
      milestone = domain.laserDonutUpdate.milestone,
      `type` = domain.laserDonutUpdate.`type`.map(DonutType.toString),
      status = domain.laserDonutUpdate.status.map(Status.toString)
    )

    val laserDonut = LaserDonutView(
      uuid = domain.laserDonut.uuid,
      goalId = domain.laserDonut.goalId,
      summary = domain.laserDonut.summary,
      description = domain.laserDonut.description,
      milestone = domain.laserDonut.milestone,
      order = domain.laserDonut.order,
      `type` = DonutType.toString(domain.laserDonut.`type`),
      status = Status.toString(domain.laserDonut.status),
      createdOn = domain.laserDonut.createdOn,
      lastModified = domain.laserDonut.lastModified,
      lastPerformed = domain.laserDonut.lastPerformed
    )

    val portionCreation = PortionCreation(
      laserDonutId = domain.portionCreation.laserDonutId,
      summary = domain.portionCreation.summary,
      status = Status.toString(domain.portionCreation.status)
    )

    val portionUpdate = PortionUpdate(
      laserDonutId = domain.portionUpdate.laserDonutId,
      summary = domain.portionUpdate.summary,
      status = domain.portionUpdate.status.map(Status.toString)
    )

    val portion = PortionView(
      uuid = domain.portion.uuid,
      laserDonutId = domain.portion.laserDonutId,
      summary = domain.portion.summary,
      order = domain.portion.order,
      status = Status.toString(domain.portion.status),
      createdOn = domain.portion.createdOn,
      lastModified = domain.portion.lastModified,
      lastPerformed = domain.portion.lastPerformed
    )

    val todoCreation = TodoCreation(
      portionId = domain.todoCreation.portionId,
      description = domain.todoCreation.description,
      status = Status.toString(domain.todoCreation.status)
    )

    val todoUpdate = TodoUpdate(
      portionId = domain.todoUpdate.portionId,
      description = domain.todoUpdate.description,
      status = domain.todoUpdate.status.map(Status.toString)
    )

    val todo = TodoView(
      uuid = domain.todo.uuid,
      portionId = domain.todo.portionId,
      description = domain.todo.description,
      order = domain.todo.order,
      status = Status.toString(domain.todo.status),
      createdOn = domain.todo.createdOn,
      lastModified = domain.todo.lastModified,
      lastPerformed = domain.todo.lastPerformed
    )

    val hobbyCreation = HobbyCreation(
      goalId = domain.hobbyCreation.goalId,
      summary = domain.hobbyCreation.summary,
      description = domain.hobbyCreation.description,
      frequency = HobbyFrequency.toString(domain.hobbyCreation.frequency),
      `type` = HobbyType.toString(domain.hobbyCreation.`type`),
      status = Status.toString(domain.hobbyCreation.status)
    )

    val hobbyUpdate = HobbyUpdate(
      goalId = domain.hobbyUpdate.goalId,
      summary = domain.hobbyUpdate.summary,
      description = domain.hobbyUpdate.description,
      frequency = domain.hobbyUpdate.frequency.map(HobbyFrequency.toString),
      `type` = domain.hobbyUpdate.`type`.map(HobbyType.toString),
      status = domain.hobbyUpdate.status.map(Status.toString)
    )

    val hobby = HobbyView(
      uuid = domain.hobby.uuid,
      goalId = domain.hobby.goalId,
      summary = domain.hobby.summary,
      description = domain.hobby.description,
      frequency = HobbyFrequency.toString(domain.hobby.frequency),
      `type` = HobbyType.toString(domain.hobby.`type`),
      status = Status.toString(domain.hobby.status),
      createdOn = domain.hobby.createdOn,
      lastModified = domain.hobby.lastModified,
      lastPerformed = domain.hobby.lastPerformed
    )

    val listUpdate = ListUpdate(
      reordered = domain.listUpdate.reordered
    )

    val tierUpsert = TierUpsert(
      laserDonuts = domain.tierUpsert.laserDonuts
    )

    val tier = TierView(
      laserDonuts = domain.tier.laserDonuts
    )

    val pyramidUpsert = PyramidOfImportanceUpsert(
      tiers = domain.pyramidUpsert.tiers.map(tier => TierUpsert(tier.laserDonuts))
    )

    val pyramid = PyramidOfImportanceView(
      tiers = domain.pyramid.tiers.map(tier => TierView(tier.laserDonuts))
    )
  }
}
