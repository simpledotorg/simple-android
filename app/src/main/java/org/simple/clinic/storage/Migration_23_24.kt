package org.simple.clinic.storage

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

@Suppress("ClassName")
class Migration_23_24 : Migration(23, 24) {

  override fun migrate(database: SupportSQLiteDatabase) {
    val createAlterTableStatement = { tableName: String ->
      """
        ALTER TABLE $tableName ADD COLUMN "deletedAt" TEXT
      """
    }

    val tablesToAddColumnTo = listOf(
        "BloodPressureMeasurement",
        "PrescribedDrug",
        "Facility",
        "MedicalHistory",
        "Appointment",
        "Communication",
        "Patient",
        "PatientAddress",
        "PatientPhoneNumber",
        "Protocol",
        "ProtocolDrug")

    tablesToAddColumnTo.map(createAlterTableStatement)
        .forEach(database::execSQL)
  }
}
