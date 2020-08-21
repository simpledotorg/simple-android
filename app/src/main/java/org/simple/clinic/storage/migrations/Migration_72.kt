package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_72 @Inject constructor() : Migration(71, 72) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        CREATE TABLE IF NOT EXISTS "TeleconsultationFacilityInfo" (
          "teleconsultationFacilityId" TEXT NOT NULL, "facilityId" TEXT NOT NULL, "syncStatus" TEXT NOT NULL,
          "createdAt" TEXT NOT NULL, "updatedAt" TEXT NOT NULL, "deletedAt" TEXT, 
          PRIMARY KEY("teleconsultationFacilityId")
        )
      """)

      execSQL("""
        CREATE TABLE IF NOT EXISTS "MedicalOfficer" (
          "medicalOfficerId" TEXT NOT NULL, "fullName" TEXT NOT NULL, "phoneNumber" TEXT NOT NULL,
          PRIMARY KEY("medicalOfficerId")
        )
      """)

      execSQL("""
        CREATE TABLE IF NOT EXISTS "TeleconsultationFacilityMedicalOfficersCrossRef" (
          "teleconsultationFacilityId" TEXT NOT NULL, "medicalOfficerId" TEXT NOT NULL,
          PRIMARY KEY("teleconsultationFacilityId", "medicalOfficerId")
        )
      """)
    }
  }
}
