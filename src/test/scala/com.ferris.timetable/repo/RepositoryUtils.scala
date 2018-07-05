package com.ferris.timetable.repo

import com.ferris.timetable.db.H2Tables
import org.scalatest.time.Span
import slick.jdbc.H2Profile.api._

import scala.concurrent.{Await, ExecutionContext}

object RepositoryUtils {

  def createOrResetTables(db: Database, dbTimeout: Span)(implicit ex: ExecutionContext): Unit = {
    val createSchemaA = H2Tables.schema.drop.cleanUp(_ => H2Tables.schema.create).asTry
    Await.result(db.run(createSchemaA), dbTimeout)
  }
}
