package org.simple.clinic.storage

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration
import org.simple.clinic.medicalhistory.MedicalHistory

/**
 * Adds [MedicalHistory.diagnosedWithHypertension] column.
 */
@Suppress("ClassName")
class Migration_14_15 : Migration(14, 15) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE `MedicalHistory`
      ADD COLUMN `diagnosedWithHypertension` INTEGER NOT NULL
      DEFAULT 0
    """)
  }
}
