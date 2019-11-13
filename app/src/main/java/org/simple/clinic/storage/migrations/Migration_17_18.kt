package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import javax.inject.Inject

/**
 * Removes foreign key references to Facility ID from [BloodPressureMeasurement] and [PrescribedDrug].
 */
@Suppress("ClassName")
class Migration_17_18 @Inject constructor() : Migration(17, 18) {

  override fun migrate(database: SupportSQLiteDatabase) {
    migrateBloodPressureMeasurement(database)
    migratePrescribedDrug(database)
  }

  private fun migrateBloodPressureMeasurement(database: SupportSQLiteDatabase) {
    // Sqlite doesn't support dropping constraints, so we recreate the table.
    database.execSQL("""ALTER TABLE "BloodPressureMeasurement" RENAME TO "BloodPressureMeasurement_v17"""")
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS "BloodPressureMeasurement"
        ("uuid" TEXT NOT NULL,
        "systolic" INTEGER NOT NULL,
        "diastolic" INTEGER NOT NULL,
        "syncStatus" TEXT NOT NULL,
        "userUuid" TEXT NOT NULL,
        "facilityUuid" TEXT NOT NULL,
        "patientUuid" TEXT NOT NULL,
        "createdAt" TEXT NOT NULL,
        "updatedAt" TEXT NOT NULL,
        PRIMARY KEY("uuid"))
    """)
    database.execSQL("""
      INSERT INTO "BloodPressureMeasurement"("uuid", "systolic", "diastolic", "syncStatus", "userUuid", "facilityUuid", "patientUuid",
        "createdAt", "updatedAt")
      SELECT "uuid", "systolic", "diastolic", "syncStatus", "userUuid", "facilityUuid", "patientUuid", "createdAt", "updatedAt"
      FROM "BloodPressureMeasurement_v17"
    """)
    database.execSQL("""DROP TABLE "BloodPressureMeasurement_v17"""")
    database.execSQL("""CREATE INDEX "index_BloodPressureMeasurement_patientUuid" ON "BloodPressureMeasurement" ("patientUuid")""")
  }

  private fun migratePrescribedDrug(database: SupportSQLiteDatabase) {
    database.execSQL("""ALTER TABLE "PrescribedDrug" RENAME TO "PrescribedDrug_v17"""")
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS "PrescribedDrug"
        ("uuid" TEXT NOT NULL,
        "name" TEXT NOT NULL,
        "dosage" TEXT,
        "rxNormCode" TEXT,
        "isDeleted" INTEGER NOT NULL,
        "isProtocolDrug" INTEGER NOT NULL,
        "patientUuid" TEXT NOT NULL,
        "facilityUuid" TEXT NOT NULL,
        "syncStatus" TEXT NOT NULL,
        "createdAt" TEXT NOT NULL,
        "updatedAt" TEXT NOT NULL,
        PRIMARY KEY("uuid"))
    """)
    database.execSQL("""
      INSERT INTO "PrescribedDrug"("uuid", "name", "dosage", "rxNormCode", "isDeleted", "isProtocolDrug", "patientUuid", "facilityUuid",
        "syncStatus", "createdAt", "updatedAt")
      SELECT "uuid", "name", "dosage", "rxNormCode", "isDeleted", "isProtocolDrug", "patientUuid", "facilityUuid", "syncStatus",
        "createdAt", "updatedAt"
      FROM "PrescribedDrug_v17"
    """)
    database.execSQL("""DROP TABLE "PrescribedDrug_v17"""")
    database.execSQL("""CREATE INDEX "index_PrescribedDrug_patientUuid" ON "PrescribedDrug" ("patientUuid")""")
  }
}
