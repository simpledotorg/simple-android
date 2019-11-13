package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration_42_43 @javax.inject.Inject constructor() : Migration(42, 43) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      DROP TABLE "Communication"
    """)
  }
}
