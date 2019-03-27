package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration_31_32 : Migration(31, 32) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE "Appointment"
      ADD COLUMN "isDefaulter" INTEGER
      DEFAULT null
    """)
  }

}
