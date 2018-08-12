package com.ferris.timetable.db

trait DatabaseComponent extends TablesComponent {

  def db: tables.profile.api.Database
}
