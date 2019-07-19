package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration_42_43 : Migration(42, 43) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      DROP TABLE "Communication"
    """)
  }
}
