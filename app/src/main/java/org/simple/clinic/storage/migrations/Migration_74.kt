package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("Classname")
class Migration_74 @Inject constructor() : Migration(73, 74) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL(
          """
            CREATE TABLE IF NOT EXISTS "TeleconsultRecord" (
            "id" TEXT NOT NULL,
            "patientId" TEXT NOT NULL, 
            "medicalOfficerId" TEXT NOT NULL,
            "createdAt" TEXT NOT NULL,
            "updatedAt" TEXT NOT NULL,
            "deletedAt" TEXT,
            "request_requesterId" TEXT,
            "request_facilityId" TEXT, 
            "request_requestedAt" TEXT,
            "record_recordedAt" TEXT,
            "record_teleconsultationType" TEXT,
            "record_patientTookMedicines" TEXT,
            "record_patientConsented" TEXT,
            "record_medicalOfficerNumber" TEXT,
             PRIMARY KEY("id")
            )
          """)

      execSQL(
          """
            CREATE TABLE IF NOT EXISTS "TeleconsultRecordPrescribedDrug" (
             "teleconsultRecordId" TEXT NOT NULL,
             "prescribedDrugUuid" TEXT NOT NULL,
             PRIMARY KEY("teleconsultRecordId")
            )
          """)
    }
  }
}
