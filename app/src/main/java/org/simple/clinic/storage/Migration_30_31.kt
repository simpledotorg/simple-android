package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration_30_31 : Migration(30, 31) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS "MissingPhoneReminder" (
        "patientUuid" TEXT NOT NULL,
        "reminded" INTEGER NOT NULL,
        PRIMARY KEY("patientUuid"))
      """)
  }
}
