package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_26_27 @Inject constructor() : Migration(26, 27) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE "Facility" ADD COLUMN "groupUuid" TEXT DEFAULT null
    """)
  }
}
