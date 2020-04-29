package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_31_32 @Inject constructor() : Migration(31, 32) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE "Appointment"
      ADD COLUMN "isDefaulter" INTEGER
      DEFAULT null
    """)
  }

}
