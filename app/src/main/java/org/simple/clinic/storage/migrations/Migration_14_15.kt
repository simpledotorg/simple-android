package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.medicalhistory.MedicalHistory
import javax.inject.Inject

/**
 * Adds [MedicalHistory.diagnosedWithHypertension] column.
 */
@Suppress("ClassName")
class Migration_14_15 @Inject constructor() : Migration(14, 15) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE `MedicalHistory`
      ADD COLUMN `diagnosedWithHypertension` INTEGER NOT NULL
      DEFAULT 0
    """)
  }
}
