package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_97 @Inject constructor() : Migration(96, 97) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        ALTER TABLE "MedicalHistory"
        ADD COLUMN "isOnDiabetesTreatment" TEXT NOT NULL DEFAULT "unknown"
    """)
    }
  }
}
