package org.simple.clinic.storage.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Suppress("ClassName")
class Migration_26_27 @javax.inject.Inject constructor() : Migration(26, 27) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE "Facility" ADD COLUMN "groupUuid" TEXT DEFAULT null
    """)
  }
}
