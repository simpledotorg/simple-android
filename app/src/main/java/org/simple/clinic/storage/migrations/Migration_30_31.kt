package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_30_31 @Inject constructor() : Migration(30, 31) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS "MissingPhoneReminder" (
        "patientUuid" TEXT NOT NULL,
        "remindedAt" TEXT NOT NULL,
        PRIMARY KEY("patientUuid"))
    """)
  }
}
