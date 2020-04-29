package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.medicalhistory.MedicalHistory
import javax.inject.Inject

/**
 * Adds [MedicalHistory]
 */
@Suppress("ClassName")
class Migration_11_12 @Inject constructor() : Migration(11, 12) {

  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("""
      CREATE TABLE IF NOT EXISTS `MedicalHistory` (
        `uuid` TEXT NOT NULL,
        `patientUuid` TEXT NOT NULL,
        `hasHadHeartAttack` INTEGER NOT NULL,
        `hasHadStroke` INTEGER NOT NULL,
        `hasHadKidneyDisease` INTEGER NOT NULL,
        `isOnTreatmentForHypertension` INTEGER NOT NULL,
        `hasDiabetes` INTEGER NOT NULL,
        `syncStatus` TEXT NOT NULL,
        `createdAt` TEXT NOT NULL,
        `updatedAt` TEXT NOT NULL,
        PRIMARY KEY(`uuid`))
    """)
  }
}
