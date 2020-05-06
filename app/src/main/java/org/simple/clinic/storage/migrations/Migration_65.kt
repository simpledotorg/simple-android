package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_65 @Inject constructor() : Migration(64, 65) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        ALTER TABLE "Facility" ADD COLUMN "config_teleconsultationEnabled" INT DEFAULT 0
      """)
    }
  }
}
