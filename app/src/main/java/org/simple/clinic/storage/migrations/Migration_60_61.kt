package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_60_61 @Inject constructor() : Migration(60, 61) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        ALTER TABLE "BloodSugarMeasurements" RENAME TO "BloodSugarMeasurements_v60"
      """)

      execSQL("""
        CREATE TABLE IF NOT EXISTS "BloodSugarMeasurements" (
            "uuid" TEXT NOT NULL,
            "reading_value" TEXT NOT NULL, "reading_type" TEXT NOT NULL,
            "recordedAt" TEXT NOT NULL, "patientUuid" TEXT NOT NULL, "userUuid" TEXT NOT NULL,
            "facilityUuid" TEXT NOT NULL, "createdAt" TEXT NOT NULL, "updatedAt" TEXT NOT NULL,
            "deletedAt" TEXT, "syncStatus" TEXT NOT NULL,
            PRIMARY KEY("uuid")
        )
      """)

      execSQL("""
        INSERT INTO "BloodSugarMeasurements" (
            "uuid", "reading_value", "reading_type", "recordedAt", "patientUuid",
            "userUuid", "facilityUuid", "createdAt", "updatedAt",
            "deletedAt", "syncStatus"
        )
        SELECT 
            "uuid", CAST ("reading_value" AS TEXT), "reading_type", "recordedAt", "patientUuid",
            "userUuid", "facilityUuid", "createdAt", "updatedAt",
            "deletedAt", "syncStatus"
        FROM "BloodSugarMeasurements_v60"
      """)

      execSQL("""DROP TABLE "BloodSugarMeasurements_v60" """)
    }
  }
}
