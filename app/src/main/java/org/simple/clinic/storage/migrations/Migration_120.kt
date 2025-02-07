package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_120 @Inject constructor() : Migration(119, 120) {

  override fun migrate(db: SupportSQLiteDatabase) {
    db.inTransaction {
      execSQL("""
        ALTER TABLE "MedicalHistory"
        ADD COLUMN "cholesterol_value" REAL DEFAULT NULL
    """.trimIndent())
    }
  }
}
