package com.ferris.timetable.db

import scala.util.Random

trait H2DatabaseComponent extends DatabaseComponent {

  import tables.profile.api.Database

  val randomSchemaName: String = "s" + Random.nextInt().toString.drop(1)
  val jdbcUrl: String = s"jdbc:h2:mem:$randomSchemaName;MODE=MySQL;DATABASE_TO_UPPER=false;INIT=CREATE SCHEMA IF NOT EXISTS $randomSchemaName;DB_CLOSE_DELAY=-1"

  override val db: Database = Database.forURL(jdbcUrl, driver = "org.h2.Driver")
}
