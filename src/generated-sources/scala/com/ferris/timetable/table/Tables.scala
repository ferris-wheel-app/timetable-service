package com.ferris.timetable.table
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
  lazy val schema: profile.SchemaDescription = MessageTable.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table MessageTable
   *  @param id Database column ID SqlType(BIGINT), AutoInc, PrimaryKey
   *  @param uuid Database column UUID SqlType(VARCHAR), Length(36,true)
   *  @param sender Database column SENDER SqlType(VARCHAR), Length(256,true)
   *  @param content Database column CONTENT SqlType(VARCHAR), Length(2000,true) */
  case class MessageRow(id: Long, uuid: String, sender: String, content: String)
  /** GetResult implicit for fetching MessageRow objects using plain SQL queries */
  implicit def GetResultMessageRow(implicit e0: GR[Long], e1: GR[String]): GR[MessageRow] = GR{
    prs => import prs._
    MessageRow.tupled((<<[Long], <<[String], <<[String], <<[String]))
  }
  /** Table description of table MESSAGE. Objects of this class serve as prototypes for rows in queries. */
  class MessageTable(_tableTag: Tag) extends profile.api.Table[MessageRow](_tableTag, "MESSAGE") {
    def * = (id, uuid, sender, content) <> (MessageRow.tupled, MessageRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(uuid), Rep.Some(sender), Rep.Some(content)).shaped.<>({r=>import r._; _1.map(_=> MessageRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(BIGINT), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column UUID SqlType(VARCHAR), Length(36,true) */
    val uuid: Rep[String] = column[String]("UUID", O.Length(36,varying=true))
    /** Database column SENDER SqlType(VARCHAR), Length(256,true) */
    val sender: Rep[String] = column[String]("SENDER", O.Length(256,varying=true))
    /** Database column CONTENT SqlType(VARCHAR), Length(2000,true) */
    val content: Rep[String] = column[String]("CONTENT", O.Length(2000,varying=true))

    /** Uniqueness Index over (uuid) (database name CONSTRAINT_INDEX_6) */
    val index1 = index("CONSTRAINT_INDEX_6", uuid, unique=true)
  }
  /** Collection-like TableQuery object for table MessageTable */
  lazy val MessageTable = new TableQuery(tag => new MessageTable(tag))
}
