package com.ferris.timetable.repo

import java.sql.Timestamp

import com.ferris.planning.config.PlanningServiceConfig
import com.ferris.planning.db.H2TablesComponent
import com.ferris.planning.scheduler.LifeSchedulerComponent
import com.ferris.planning.utils.TimerComponent

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

trait H2PlanningRepositoryComponent extends SqlPlanningRepositoryComponent with H2TablesComponent with TimerComponent with LifeSchedulerComponent {

  override implicit val repoEc: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val randomSchemaName: String = "s" + Random.nextInt().toString.drop(1)
  val jdbcUrl: String = s"jdbc:h2:mem:$randomSchemaName;MODE=MySQL;DATABASE_TO_UPPER=false;INIT=CREATE SCHEMA IF NOT EXISTS $randomSchemaName;DB_CLOSE_DELAY=-1"
  import tables.profile.api._
  override val db: Database = Database.forURL(jdbcUrl, driver = "org.h2.Driver")

  val config: PlanningServiceConfig

  override val repo = new H2PlanningRepository

  class H2PlanningRepository extends SqlPlanningRepository(config) {
    def getScheduledLaserDonuts: Future[Seq[tables.ScheduledLaserDonutRow]] = {
      db.run(tables.ScheduledLaserDonutTable.result)
    }

    def insertCurrentActivity(currentLaserDonutId: Long, currentPortionId: Long, lastWeeklyUpdate: Timestamp, lastDailyUpdate: Timestamp): Future[Int] = {
      val row = tables.CurrentActivityRow(
        id = 0L,
        currentLaserDonut = currentLaserDonutId,
        currentPortion = currentPortionId,
        lastDailyUpdate = lastDailyUpdate,
        lastWeeklyUpdate = lastWeeklyUpdate
      )
      val action = tables.CurrentActivityTable += row
      db.run(action)
    }

    def getCurrentActivity: Future[Option[tables.CurrentActivityRow]] = {
      val action = tables.CurrentActivityTable.result.headOption
      db.run(action)
    }
  }
}
