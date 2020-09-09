package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_76 @Inject constructor(): Migration(75, 76) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        CREATE TABLE IF NOT EXISTS "OverdueAppointment_New" (
            "appointmentId" TEXT NOT NULL, "patientId" TEXT NOT NULL, "facilityId" TEXT NOT NULL, 
            "scheduledDate" TEXT NOT NULL, "fullName" TEXT NOT NULL, "gender" TEXT NOT NULL, 
            "dateOfBirth" TEXT, "phoneNumber" TEXT, "isAtHighRisk" INTEGER NOT NULL, "patientLastSeen" TEXT NOT NULL, 
            "diagnosedWithDiabetes" TEXT, "diagnosedWithHypertension" TEXT, "patientAssignedFacilityUuid" TEXT, 
            "appointmentFacilityName" TEXT, "age_value" INTEGER, "age_updatedAt" TEXT, "patient_address_streetAddress" TEXT, 
            "patient_address_colonyOrVillage" TEXT, "patient_address_district" TEXT NOT NULL, "patient_address_state" TEXT NOT NULL, 
        PRIMARY KEY("appointmentId")
      )
      """)
    }
  }
}
