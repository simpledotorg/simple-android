package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.util.UtcClock
import java.time.Instant
import javax.inject.Inject

@Suppress("ClassName")
class Migration_34_35 @Inject constructor(
    private val utcClock: UtcClock
) : Migration(34, 35) {

  override fun migrate(database: SupportSQLiteDatabase) {
    val createAlterTableStatement = { tableName: String ->
      """
        ALTER TABLE $tableName ADD COLUMN "recordedAt" TEXT NOT NULL DEFAULT '0'
      """
    }

    val setDefaultValue = { tableName: String ->
      """
        UPDATE $tableName SET recordedAt = createdAt
      """
    }

    val tablesToAddColumnTo = listOf(
        "BloodPressureMeasurement",
        "PrescribedDrug",
        "MedicalHistory",
        "Appointment",
        "Patient",
        "PatientAddress",
        "PatientPhoneNumber",
        "BusinessId")


    tablesToAddColumnTo.map(createAlterTableStatement)
        .forEach(database::execSQL)

    tablesToAddColumnTo.map(setDefaultValue)
        .forEach(database::execSQL)

    database.execSQL("""
      UPDATE "Patient" AS P
      SET recordedAt = MIN(
        IFNULL((SELECT MIN(createdAt)
                FROM "BloodPressureMeasurement"
                WHERE patientUuid = P.uuid
                AND deletedAt IS NULL), P.createdAt),
        P.createdAt)
    """)

    val now = Instant.now(utcClock)

    database.execSQL("""
      UPDATE "Patient"
      SET syncStatus = 'PENDING', updatedAt = '$now'
      WHERE uuid IN (
        SELECT patientUuid FROM "BloodPressureMeasurement" WHERE syncStatus = 'PENDING' AND deletedAt IS NULL GROUP BY patientUuid
        ) AND recordedAt != createdAt
    """)
  }
}
