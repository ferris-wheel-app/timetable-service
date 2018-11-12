package com.ferris.timetable.db
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.MySQLProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = RoutineTable.schema ++ RoutineTimeBlockTable.schema ++ ScheduledTimeBlockTable.schema ++ TimeBlockTable.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table RoutineTable
   *  @param id Database column ID SqlType(BIGINT), AutoInc, PrimaryKey
   *  @param uuid Database column UUID SqlType(VARCHAR), Length(36,true)
   *  @param name Database column NAME SqlType(VARCHAR), Length(256,true)
   *  @param isCurrent Database column IS_CURRENT SqlType(TINYINT) */
  final case class RoutineRow(id: Long, uuid: String, name: String, isCurrent: Byte)
  /** GetResult implicit for fetching RoutineRow objects using plain SQL queries */
  implicit def GetResultRoutineRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Byte]): GR[RoutineRow] = GR{
    prs => import prs._
    RoutineRow.tupled((<<[Long], <<[String], <<[String], <<[Byte]))
  }
  /** Table description of table ROUTINE. Objects of this class serve as prototypes for rows in queries. */
  class RoutineTable(_tableTag: Tag) extends profile.api.Table[RoutineRow](_tableTag, "ROUTINE") {
    def * = (id, uuid, name, isCurrent) <> (RoutineRow.tupled, RoutineRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(uuid), Rep.Some(name), Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> RoutineRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(BIGINT), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column UUID SqlType(VARCHAR), Length(36,true) */
    val uuid: Rep[String] = column[String]("UUID", O.Length(36,varying=true))
    /** Database column NAME SqlType(VARCHAR), Length(256,true) */
    val name: Rep[String] = column[String]("NAME", O.Length(256,varying=true))
    /** Database column IS_CURRENT SqlType(TINYINT) */
    val isCurrent: Rep[Byte] = column[Byte]("IS_CURRENT")

    /** Uniqueness Index over (uuid) (database name CONSTRAINT_INDEX_7) */
    val index1 = index("CONSTRAINT_INDEX_7", uuid, unique=true)
  }
  /** Collection-like TableQuery object for table RoutineTable */
  lazy val RoutineTable = new TableQuery(tag => new RoutineTable(tag))

  /** Entity class storing rows of table RoutineTimeBlockTable
   *  @param id Database column ID SqlType(BIGINT), AutoInc, PrimaryKey
   *  @param routineId Database column ROUTINE_ID SqlType(BIGINT)
   *  @param timeBlockId Database column TIME_BLOCK_ID SqlType(BIGINT)
   *  @param dayOfWeek Database column DAY_OF_WEEK SqlType(VARCHAR), Length(36,true) */
  final case class RoutineTimeBlockRow(id: Long, routineId: Long, timeBlockId: Long, dayOfWeek: String)
  /** GetResult implicit for fetching RoutineTimeBlockRow objects using plain SQL queries */
  implicit def GetResultRoutineTimeBlockRow(implicit e0: GR[Long], e1: GR[String]): GR[RoutineTimeBlockRow] = GR{
    prs => import prs._
    RoutineTimeBlockRow.tupled((<<[Long], <<[Long], <<[Long], <<[String]))
  }
  /** Table description of table ROUTINE_TIME_BLOCK. Objects of this class serve as prototypes for rows in queries. */
  class RoutineTimeBlockTable(_tableTag: Tag) extends profile.api.Table[RoutineTimeBlockRow](_tableTag, "ROUTINE_TIME_BLOCK") {
    def * = (id, routineId, timeBlockId, dayOfWeek) <> (RoutineTimeBlockRow.tupled, RoutineTimeBlockRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(routineId), Rep.Some(timeBlockId), Rep.Some(dayOfWeek)).shaped.<>({r=>import r._; _1.map(_=> RoutineTimeBlockRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(BIGINT), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column ROUTINE_ID SqlType(BIGINT) */
    val routineId: Rep[Long] = column[Long]("ROUTINE_ID")
    /** Database column TIME_BLOCK_ID SqlType(BIGINT) */
    val timeBlockId: Rep[Long] = column[Long]("TIME_BLOCK_ID")
    /** Database column DAY_OF_WEEK SqlType(VARCHAR), Length(36,true) */
    val dayOfWeek: Rep[String] = column[String]("DAY_OF_WEEK", O.Length(36,varying=true))

    /** Foreign key referencing RoutineTable (database name ROUTINE_FK) */
    lazy val routineTableFk = foreignKey("ROUTINE_FK", routineId, RoutineTable)(r => r.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)
    /** Foreign key referencing TimeBlockTable (database name TIME_BLOCK_FK_1) */
    lazy val timeBlockTableFk = foreignKey("TIME_BLOCK_FK_1", timeBlockId, TimeBlockTable)(r => r.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)
  }
  /** Collection-like TableQuery object for table RoutineTimeBlockTable */
  lazy val RoutineTimeBlockTable = new TableQuery(tag => new RoutineTimeBlockTable(tag))

  /** Entity class storing rows of table ScheduledTimeBlockTable
   *  @param id Database column ID SqlType(BIGINT), AutoInc, PrimaryKey
   *  @param date Database column DATE SqlType(DATE)
   *  @param startTime Database column START_TIME SqlType(TIME)
   *  @param finishTime Database column FINISH_TIME SqlType(TIME)
   *  @param taskType Database column TASK_TYPE SqlType(VARCHAR), Length(36,true)
   *  @param taskId Database column TASK_ID SqlType(VARCHAR), Length(36,true)
   *  @param isDone Database column IS_DONE SqlType(TINYINT), Default(0) */
  final case class ScheduledTimeBlockRow(id: Long, date: java.sql.Date, startTime: java.sql.Time, finishTime: java.sql.Time, taskType: String, taskId: String, isDone: Byte = 0)
  /** GetResult implicit for fetching ScheduledTimeBlockRow objects using plain SQL queries */
  implicit def GetResultScheduledTimeBlockRow(implicit e0: GR[Long], e1: GR[java.sql.Date], e2: GR[java.sql.Time], e3: GR[String], e4: GR[Byte]): GR[ScheduledTimeBlockRow] = GR{
    prs => import prs._
    ScheduledTimeBlockRow.tupled((<<[Long], <<[java.sql.Date], <<[java.sql.Time], <<[java.sql.Time], <<[String], <<[String], <<[Byte]))
  }
  /** Table description of table SCHEDULED_TIME_BLOCK. Objects of this class serve as prototypes for rows in queries. */
  class ScheduledTimeBlockTable(_tableTag: Tag) extends profile.api.Table[ScheduledTimeBlockRow](_tableTag, "SCHEDULED_TIME_BLOCK") {
    def * = (id, date, startTime, finishTime, taskType, taskId, isDone) <> (ScheduledTimeBlockRow.tupled, ScheduledTimeBlockRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(date), Rep.Some(startTime), Rep.Some(finishTime), Rep.Some(taskType), Rep.Some(taskId), Rep.Some(isDone)).shaped.<>({r=>import r._; _1.map(_=> ScheduledTimeBlockRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(BIGINT), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column DATE SqlType(DATE) */
    val date: Rep[java.sql.Date] = column[java.sql.Date]("DATE")
    /** Database column START_TIME SqlType(TIME) */
    val startTime: Rep[java.sql.Time] = column[java.sql.Time]("START_TIME")
    /** Database column FINISH_TIME SqlType(TIME) */
    val finishTime: Rep[java.sql.Time] = column[java.sql.Time]("FINISH_TIME")
    /** Database column TASK_TYPE SqlType(VARCHAR), Length(36,true) */
    val taskType: Rep[String] = column[String]("TASK_TYPE", O.Length(36,varying=true))
    /** Database column TASK_ID SqlType(VARCHAR), Length(36,true) */
    val taskId: Rep[String] = column[String]("TASK_ID", O.Length(36,varying=true))
    /** Database column IS_DONE SqlType(TINYINT), Default(0) */
    val isDone: Rep[Byte] = column[Byte]("IS_DONE", O.Default(0))
  }
  /** Collection-like TableQuery object for table ScheduledTimeBlockTable */
  lazy val ScheduledTimeBlockTable = new TableQuery(tag => new ScheduledTimeBlockTable(tag))

  /** Entity class storing rows of table TimeBlockTable
   *  @param id Database column ID SqlType(BIGINT), AutoInc, PrimaryKey
   *  @param startTime Database column START_TIME SqlType(TIME)
   *  @param finishTime Database column FINISH_TIME SqlType(TIME)
   *  @param taskType Database column TASK_TYPE SqlType(VARCHAR), Length(36,true)
   *  @param taskId Database column TASK_ID SqlType(VARCHAR), Length(36,true) */
  final case class TimeBlockRow(id: Long, startTime: java.sql.Time, finishTime: java.sql.Time, taskType: String, taskId: Option[String])
  /** GetResult implicit for fetching TimeBlockRow objects using plain SQL queries */
  implicit def GetResultTimeBlockRow(implicit e0: GR[Long], e1: GR[java.sql.Time], e2: GR[String], e3: GR[Option[String]]): GR[TimeBlockRow] = GR{
    prs => import prs._
    TimeBlockRow.tupled((<<[Long], <<[java.sql.Time], <<[java.sql.Time], <<[String], <<?[String]))
  }
  /** Table description of table TIME_BLOCK. Objects of this class serve as prototypes for rows in queries. */
  class TimeBlockTable(_tableTag: Tag) extends profile.api.Table[TimeBlockRow](_tableTag, "TIME_BLOCK") {
    def * = (id, startTime, finishTime, taskType, taskId) <> (TimeBlockRow.tupled, TimeBlockRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(startTime), Rep.Some(finishTime), Rep.Some(taskType), taskId).shaped.<>({r=>import r._; _1.map(_=> TimeBlockRow.tupled((_1.get, _2.get, _3.get, _4.get, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(BIGINT), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column START_TIME SqlType(TIME) */
    val startTime: Rep[java.sql.Time] = column[java.sql.Time]("START_TIME")
    /** Database column FINISH_TIME SqlType(TIME) */
    val finishTime: Rep[java.sql.Time] = column[java.sql.Time]("FINISH_TIME")
    /** Database column TASK_TYPE SqlType(VARCHAR), Length(36,true) */
    val taskType: Rep[String] = column[String]("TASK_TYPE", O.Length(36,varying=true))
    /** Database column TASK_ID SqlType(VARCHAR), Length(36,true) */
    val taskId: Rep[Option[String]] = column[Option[String]]("TASK_ID", O.Length(36,varying=true))
  }
  /** Collection-like TableQuery object for table TimeBlockTable */
  lazy val TimeBlockTable = new TableQuery(tag => new TimeBlockTable(tag))
}
