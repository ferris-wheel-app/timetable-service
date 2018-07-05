package com.ferris.timetable.db

import com.ferris.timetable.table.Tables

import slick.jdbc.MySQLProfile

object MySQLTables extends Tables {
  override val profile = MySQLProfile
}

trait MySQLTablesComponent extends TablesComponent {
  val tables = MySQLTables
}
