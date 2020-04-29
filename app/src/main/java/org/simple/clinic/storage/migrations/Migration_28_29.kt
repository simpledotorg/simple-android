package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_28_29 @Inject constructor() : Migration(28, 29) {

  override fun migrate(database: SupportSQLiteDatabase) {
    val tableNames = listOf(
        "Patient",
        "BloodPressureMeasurement",
        "PrescribedDrug",
        "Facility",
        "Appointment",
        "Communication",
        "MedicalHistory",
        "Protocol"
    )
    tableNames.forEach { tableName ->
      database.execSQL("""
          UPDATE "$tableName" SET "syncStatus" = 'PENDING' WHERE "syncStatus" = 'IN_FLIGHT'
      """)
    }
  }
}
