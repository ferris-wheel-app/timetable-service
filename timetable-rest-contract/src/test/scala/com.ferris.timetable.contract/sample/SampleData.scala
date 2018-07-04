package com.ferris.planning.contract.sample

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import com.ferris.planning.contract.resource.Resources.In._
import com.ferris.planning.contract.resource.Resources.Out._

object SampleData {

  private val currentYear = LocalDate.now
  private val nextYear = currentYear.plusYears(1)

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

  val backlogItemCreation = BacklogItemCreation(
    summary = "I need to get my shit together",
    description = "I need to get my shit together",
    `type` = "issue"
  )

  val backlogItemUpdate = BacklogItemUpdate(
    summary = Some("I want to be the best of the best at programming"),
    description = Some("I want to be the best of the best at programming"),
    `type` = Some("idea")
  )

  val backlogItem = BacklogItemView(
    uuid = UUID.randomUUID,
    summary = "I need to get my shit together",
    description = "I need to get my shit together",
    `type` = "issue",
    createdOn = LocalDateTime.now,
    lastModified = Some(LocalDateTime.now)
  )

  val epochCreation = EpochCreation(
    name = "Messinaissance",
    totem = "Hero",
    question = "Am I capable of becoming an Übermensch?"
  )

  val epochUpdate = EpochUpdate(
    name = Some("Wakanda"),
    totem = Some("Leader"),
    question = Some("Is Africa capable of achieving full development?")
  )

  val epoch = EpochView(
    uuid = UUID.randomUUID,
    name = "Messinaissance",
    totem = "Hero",
    question = "Am I capable of becoming an Übermensch?",
    createdOn = LocalDateTime.now,
    lastModified = Some(LocalDateTime.now)
  )

  val yearCreation = YearCreation(
    epochId = UUID.randomUUID,
    startDate = currentYear
  )

  val yearUpdate = YearUpdate(
    epochId = Some(UUID.randomUUID),
    startDate = Some(currentYear.plusYears(2))
  )

  val year = YearView(
    uuid = UUID.randomUUID,
    epochId = UUID.randomUUID,
    startDate = currentYear,
    finishDate = nextYear,
    createdOn = LocalDateTime.now,
    lastModified = Some(LocalDateTime.now)
  )

  val themeCreation = ThemeCreation(
    yearId = UUID.randomUUID,
    name = "Career Capital"
  )

  val themeUpdate = ThemeUpdate(
    yearId = Some(UUID.randomUUID),
    name = Some("Mission")
  )

  val theme = ThemeView(
    uuid = UUID.randomUUID,
    yearId = UUID.randomUUID,
    name = "Career Capital",
    createdOn = LocalDateTime.now,
    lastModified = Some(LocalDateTime.now)
  )

  val goalCreation = GoalCreation(
    themeId = UUID.randomUUID,
    backlogItems = Nil,
    summary = "Master at least one foreign language",
    description = "Learn French, Italian, and Korean",
    level = 1,
    priority = false,
    graduation = "hobby",
    status = "not_achieved"
  )

  val goalUpdate = GoalUpdate(
    themeId = Some(UUID.randomUUID),
    backlogItems = Some(Nil),
    summary = Some("Learn to play an instrument"),
    description = Some("Learn to play the piano, the guitar, and the saxophone"),
    level = Some(2),
    priority = Some(false),
    graduation = Some("abandoned"),
    status = Some("employed")
  )

  val goal = GoalView(
    uuid = UUID.randomUUID,
    themeId = UUID.randomUUID,
    backlogItems = UUID.randomUUID :: UUID.randomUUID :: Nil,
    summary = "Master at least one foreign language",
    description = "Learn French, Italian, and Korean",
    level = 1,
    priority = false,
    graduation = "hobby",
    status = "not_achieved",
    createdOn = LocalDateTime.now,
    lastModified = Some(LocalDateTime.now)
  )

  val threadCreation = ThreadCreation(
    goalId = Some(UUID.randomUUID),
    summary = "Go for a run",
    description = "Go for a run",
    status = "planned"
  )

  val threadUpdate = ThreadUpdate(
    goalId = Some(UUID.randomUUID),
    summary = Some("Sleep"),
    description = Some("Sleep for 8 hours"),
    status = Some("in_progress")
  )

  val thread = ThreadView(
    uuid = UUID.randomUUID,
    goalId = Some(UUID.randomUUID),
    summary = "Go for a run",
    description = "Go for a run",
    status = "planned",
    createdOn = LocalDateTime.now,
    lastModified = Some(LocalDateTime.now),
    lastPerformed = Some(LocalDateTime.now)
  )

  val weaveCreation = WeaveCreation(
    goalId = Some(UUID.randomUUID),
    summary = "Organise a tech lecture",
    description = "Create a presentation about Kafka",
    `type` = "pdr",
    status = "planned"
  )

  val weaveUpdate = WeaveUpdate(
    goalId = Some(UUID.randomUUID),
    summary = Some("Apply your new-found Go knowledge"),
    description = Some("Create a snuffleupagus"),
    `type` = Some("bau"),
    status = Some("complete")
  )

  val weave = WeaveView(
    uuid = UUID.randomUUID,
    goalId = Some(UUID.randomUUID),
    summary = "Organise a tech lecture",
    description = "Create a presentation about Kafka",
    `type` = "pdr",
    status = "planned",
    createdOn = LocalDateTime.now,
    lastModified = Some(LocalDateTime.now),
    lastPerformed = Some(LocalDateTime.now)
  )

  val laserDonutCreation = LaserDonutCreation(
    goalId = UUID.randomUUID,
    summary = "Implement initial microservices",
    description = "Implement planning-service, timetable-service, and history-service, in a microservices-based architecture",
    milestone = "A deployed backend service",
    `type` = "project_focused",
    status = "in_progress"
  )

  val laserDonutUpdate = LaserDonutUpdate(
    goalId = Some(UUID.randomUUID),
    summary = Some("Create the front-end"),
    description = Some("Use React"),
    milestone = Some("A basic working prototype"),
    `type` = Some("project_focused"),
    status = Some("in_progress")
  )

  val laserDonut = LaserDonutView(
    uuid = UUID.randomUUID,
    goalId = UUID.randomUUID,
    summary = "Implement initial microservices",
    description = "Implement planning-service, timetable-service, and history-service, in a microservices-based architecture",
    milestone = "A basic working prototype",
    order = 1,
    `type` = "skill_focused",
    status = "planned",
    createdOn = LocalDateTime.now,
    lastModified = Some(LocalDateTime.now),
    lastPerformed = Some(LocalDateTime.now)
  )

  val portionCreation = PortionCreation(
    laserDonutId = UUID.randomUUID,
    summary = "Write tests",
    status = "in_progress"
  )

  val portionUpdate = PortionUpdate(
    laserDonutId = Some(UUID.randomUUID),
    summary = Some("Split into sub-projects"),
    status = Some("in_progress")
  )

  val portion = PortionView(
    uuid = UUID.randomUUID,
    laserDonutId = UUID.randomUUID,
    summary = "Write tests",
    order = 13,
    status = "in_progress",
    createdOn = LocalDateTime.now,
    lastModified = Some(LocalDateTime.now),
    lastPerformed = Some(LocalDateTime.now)
  )

  val todoCreation = TodoCreation(
    portionId = UUID.randomUUID,
    description = "Create sample data for tests",
    status = "complete"
  )

  val todoUpdate = TodoUpdate(
    portionId = Some(UUID.randomUUID),
    description = Some("Create repository tests"),
    status = Some("complete")
  )

  val todo = TodoView(
    uuid = UUID.randomUUID,
    portionId = UUID.randomUUID,
    description = "Create sample data for tests",
    order = 4,
    status = "complete",
    createdOn = LocalDateTime.now,
    lastModified = Some(LocalDateTime.now),
    lastPerformed = Some(LocalDateTime.now)
  )

  val hobbyCreation = HobbyCreation(
    goalId = Some(UUID.randomUUID),
    summary = "Yoga",
    description = "Train in Acro-Yoga",
    frequency = "continuous",
    `type` = "active",
    status = "complete"
  )

  val hobbyUpdate = HobbyUpdate(
    goalId = Some(UUID.randomUUID),
    summary = Some("Play ping-pong"),
    description = Some("Table tennis"),
    frequency = Some("continuous"),
    `type` = Some("active"),
    status = Some("complete")
  )

  val hobby = HobbyView(
    uuid = UUID.randomUUID,
    goalId = Some(UUID.randomUUID),
    summary = "Yoga",
    description = "Train in Acro-Yoga",
    frequency = "continuous",
    `type` = "active",
    status = "complete",
    createdOn = LocalDateTime.now,
    lastModified = Some(LocalDateTime.now),
    lastPerformed = Some(LocalDateTime.now)
  )
}
