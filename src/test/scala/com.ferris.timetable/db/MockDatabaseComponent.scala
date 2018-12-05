package com.ferris.timetable.db

import org.scalatest.mockito.MockitoSugar.mock

trait MockDatabaseComponent extends DatabaseComponent {
  import tables.profile.api.Database
  override val db: Database = mock[Database]
}
