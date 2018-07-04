package com.ferris.timetable.client

import java.util.UUID

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Path._
import akka.stream.ActorMaterializer
import com.ferris.service.client.{HttpServer, ServiceClient}

import scala.concurrent.Future

class PlanningServiceClient(val server: HttpServer, implicit val mat: ActorMaterializer) extends ServiceClient with PlanningRestFormats {

  def this(server: HttpServer) = this(server, server.mat)

  private val apiPath = /("api")

  private val messagesPath = "messages"
  private val backlogItemsPath = "backlog-items"
  private val epochsPath = "epochs"
  private val yearsPath = "years"
  private val themesPath = "themes"
  private val goalsPath = "goals"
  private val threadsPath = "threads"
  private val weavesPath = "weaves"
  private val laserDonutsPath = "laser-donuts"
  private val portionsPath = "portions"
  private val todosPath = "todos"
  private val hobbiesPath = "hobbies"

  def createMessage(creation: MessageCreation): Future[MessageView] =
    makePostRequest[MessageCreation, MessageView](Uri(path = apiPath / messagesPath), creation)

  def createBacklogItem(creation: BacklogItemCreation): Future[BacklogItemView] =
    makePostRequest[BacklogItemCreation, BacklogItemView](Uri(path = apiPath / backlogItemsPath), creation)

  def createEpoch(creation: EpochCreation): Future[EpochView] =
    makePostRequest[EpochCreation, EpochView](Uri(path = apiPath / epochsPath), creation)

  def createYear(creation: YearCreation): Future[YearView] =
    makePostRequest[YearCreation, YearView](Uri(path = apiPath / yearsPath), creation)

  def createTheme(creation: ThemeCreation): Future[ThemeView] =
    makePostRequest[ThemeCreation, ThemeView](Uri(path = apiPath / themesPath), creation)

  def createGoal(creation: GoalCreation): Future[GoalView] =
    makePostRequest[GoalCreation, GoalView](Uri(path = apiPath / goalsPath), creation)

  def createThread(creation: ThreadCreation): Future[ThreadView] =
    makePostRequest[ThreadCreation, ThreadView](Uri(path = apiPath / threadsPath), creation)

  def createWeave(creation: WeaveCreation): Future[WeaveView] =
    makePostRequest[WeaveCreation, WeaveView](Uri(path = apiPath / weavesPath), creation)

  def createLaserDonut(creation: LaserDonutCreation): Future[LaserDonutView] =
    makePostRequest[LaserDonutCreation, LaserDonutView](Uri(path = apiPath / laserDonutsPath), creation)

  def createPortion(creation: PortionCreation): Future[PortionView] =
    makePostRequest[PortionCreation, PortionView](Uri(path = apiPath / portionsPath), creation)

  def createTodo(creation: TodoCreation): Future[TodoView] =
    makePostRequest[TodoCreation, TodoView](Uri(path = apiPath / todosPath), creation)

  def createHobby(creation: HobbyCreation): Future[HobbyView] =
    makePostRequest[HobbyCreation, HobbyView](Uri(path = apiPath / hobbiesPath), creation)


  def updateMessage(id: UUID, update: MessageUpdate): Future[MessageView] =
    makePutRequest[MessageUpdate, MessageView](Uri(path = apiPath / messagesPath / id.toString), update)

  def updateBacklogItem(id: UUID, update: BacklogItemUpdate): Future[BacklogItemView] =
    makePutRequest[BacklogItemUpdate, BacklogItemView](Uri(path = apiPath / backlogItemsPath / id.toString), update)

  def updateEpoch(id: UUID, update: EpochUpdate): Future[EpochView] =
    makePutRequest[EpochUpdate, EpochView](Uri(path = apiPath / epochsPath / id.toString), update)

  def updateYear(id: UUID, update: YearUpdate): Future[YearView] =
    makePutRequest[YearUpdate, YearView](Uri(path = apiPath / yearsPath / id.toString), update)

  def updateTheme(id: UUID, update: ThemeUpdate): Future[ThemeView] =
    makePutRequest[ThemeUpdate, ThemeView](Uri(path = apiPath / themesPath / id.toString), update)

  def updateGoal(id: UUID, update: GoalUpdate): Future[GoalView] =
    makePutRequest[GoalUpdate, GoalView](Uri(path = apiPath / goalsPath / id.toString), update)

  def updateThread(id: UUID, update: ThreadUpdate): Future[ThreadView] =
    makePutRequest[ThreadUpdate, ThreadView](Uri(path = apiPath / threadsPath / id.toString), update)

  def updateWeave(id: UUID, update: WeaveUpdate): Future[WeaveView] =
    makePutRequest[WeaveUpdate, WeaveView](Uri(path = apiPath / weavesPath / id.toString), update)

  def updateLaserDonut(id: UUID, update: LaserDonutUpdate): Future[LaserDonutView] =
    makePutRequest[LaserDonutUpdate, LaserDonutView](Uri(path = apiPath / laserDonutsPath / id.toString), update)

  def updatePortion(id: UUID, update: PortionUpdate): Future[PortionView] =
    makePutRequest[PortionUpdate, PortionView](Uri(path = apiPath / portionsPath / id.toString), update)

  def updateTodo(id: UUID, update: TodoUpdate): Future[TodoView] =
    makePutRequest[TodoUpdate, TodoView](Uri(path = apiPath / todosPath / id.toString), update)

  def updateHobby(id: UUID, update: HobbyUpdate): Future[HobbyView] =
    makePutRequest[HobbyUpdate, HobbyView](Uri(path = apiPath / hobbiesPath / id.toString), update)


  def message(id: UUID): Future[Option[MessageView]] =
    makeGetRequest[Option[MessageView]](Uri(path = apiPath / messagesPath / id.toString))

  def backlogItem(id: UUID): Future[Option[BacklogItemView]] =
    makeGetRequest[Option[BacklogItemView]](Uri(path = apiPath / backlogItemsPath / id.toString))

  def epoch(id: UUID): Future[Option[EpochView]] =
    makeGetRequest[Option[EpochView]](Uri(path = apiPath / epochsPath / id.toString))

  def year(id: UUID): Future[Option[YearView]] =
    makeGetRequest[Option[YearView]](Uri(path = apiPath / yearsPath / id.toString))

  def theme(id: UUID): Future[Option[ThemeView]] =
    makeGetRequest[Option[ThemeView]](Uri(path = apiPath / themesPath / id.toString))

  def goal(id: UUID): Future[Option[GoalView]] =
    makeGetRequest[Option[GoalView]](Uri(path = apiPath / goalsPath / id.toString))

  def thread(id: UUID): Future[Option[ThreadView]] =
    makeGetRequest[Option[ThreadView]](Uri(path = apiPath / threadsPath / id.toString))

  def weave(id: UUID): Future[Option[WeaveView]] =
    makeGetRequest[Option[WeaveView]](Uri(path = apiPath / weavesPath / id.toString))

  def laserDonut(id: UUID): Future[Option[LaserDonutView]] =
    makeGetRequest[Option[LaserDonutView]](Uri(path = apiPath / laserDonutsPath / id.toString))

  def portion(id: UUID): Future[Option[PortionView]] =
    makeGetRequest[Option[PortionView]](Uri(path = apiPath / portionsPath / id.toString))

  def todo(id: UUID): Future[Option[TodoView]] =
    makeGetRequest[Option[TodoView]](Uri(path = apiPath / todosPath / id.toString))

  def hobby(id: UUID): Future[Option[HobbyView]] =
    makeGetRequest[Option[HobbyView]](Uri(path = apiPath / hobbiesPath / id.toString))


  def messages: Future[List[MessageView]] =
    makeGetRequest[List[MessageView]](Uri(path = apiPath / messagesPath))

  def backlogItems: Future[List[BacklogItemView]] =
    makeGetRequest[List[BacklogItemView]](Uri(path = apiPath / backlogItemsPath))

  def epochs: Future[List[EpochView]] =
    makeGetRequest[List[EpochView]](Uri(path = apiPath / epochsPath))

  def years: Future[List[YearView]] =
    makeGetRequest[List[YearView]](Uri(path = apiPath / yearsPath))

  def themes: Future[List[ThemeView]] =
    makeGetRequest[List[ThemeView]](Uri(path = apiPath / themesPath))

  def goals: Future[List[GoalView]] =
    makeGetRequest[List[GoalView]](Uri(path = apiPath / goalsPath))

  def threads: Future[List[ThreadView]] =
    makeGetRequest[List[ThreadView]](Uri(path = apiPath / threadsPath))

  def weaves: Future[List[WeaveView]] =
    makeGetRequest[List[WeaveView]](Uri(path = apiPath / weavesPath))

  def laserDonuts: Future[List[LaserDonutView]] =
    makeGetRequest[List[LaserDonutView]](Uri(path = apiPath / laserDonutsPath))

  def portions: Future[List[PortionView]] =
    makeGetRequest[List[PortionView]](Uri(path = apiPath / portionsPath))

  def todos: Future[List[TodoView]] =
    makeGetRequest[List[TodoView]](Uri(path = apiPath / todosPath))

  def hobbies: Future[List[HobbyView]] =
    makeGetRequest[List[HobbyView]](Uri(path = apiPath / hobbiesPath))


  def deleteMessage(id: UUID): Future[DeletionResult] =
    makeDeleteRequest[DeletionResult](Uri(path = apiPath / messagesPath / id.toString))

  def deleteBacklogItem(id: UUID): Future[DeletionResult] =
    makeDeleteRequest[DeletionResult](Uri(path = apiPath / backlogItemsPath / id.toString))

  def deleteEpoch(id: UUID): Future[DeletionResult] =
    makeDeleteRequest[DeletionResult](Uri(path = apiPath / epochsPath / id.toString))

  def deleteYear(id: UUID): Future[DeletionResult] =
    makeDeleteRequest[DeletionResult](Uri(path = apiPath / yearsPath / id.toString))

  def deleteTheme(id: UUID): Future[DeletionResult] =
    makeDeleteRequest[DeletionResult](Uri(path = apiPath / themesPath / id.toString))

  def deleteGoal(id: UUID): Future[DeletionResult] =
    makeDeleteRequest[DeletionResult](Uri(path = apiPath / goalsPath / id.toString))

  def deleteThread(id: UUID): Future[DeletionResult] =
    makeDeleteRequest[DeletionResult](Uri(path = apiPath / threadsPath / id.toString))

  def deleteWeave(id: UUID): Future[DeletionResult] =
    makeDeleteRequest[DeletionResult](Uri(path = apiPath / weavesPath / id.toString))

  def deleteLaserDonut(id: UUID): Future[DeletionResult] =
    makeDeleteRequest[DeletionResult](Uri(path = apiPath / laserDonutsPath / id.toString))

  def deletePortion(id: UUID): Future[DeletionResult] =
    makeDeleteRequest[DeletionResult](Uri(path = apiPath / portionsPath / id.toString))

  def deleteTodo(id: UUID): Future[DeletionResult] =
    makeDeleteRequest[DeletionResult](Uri(path = apiPath / todosPath / id.toString))

  def deleteHobby(id: UUID): Future[DeletionResult] =
    makeDeleteRequest[DeletionResult](Uri(path = apiPath / hobbiesPath / id.toString))
}
