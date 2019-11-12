package org.simple.clinic.storage

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Suppress("ClassName")
class Migration_23_24 @javax.inject.Inject constructor() : Migration(23, 24) {

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
