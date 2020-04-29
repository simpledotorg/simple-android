package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_47_48 @Inject constructor() : Migration(47, 48) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE "Patient" ADD COLUMN "reminderConsent" TEXT NOT NULL DEFAULT "granted"
    """)
  }
}
