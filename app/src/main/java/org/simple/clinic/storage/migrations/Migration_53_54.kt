package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_53_54 @Inject constructor() : Migration(53, 54) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL(""" DROP INDEX "index_BloodPressureMeasurement_patientUuid" """)

      execSQL(""" ALTER TABLE "BloodPressureMeasurement" RENAME TO "BloodPressureMeasurement_v53" """)

      execSQL("""
        CREATE TABLE IF NOT EXISTS "BloodPressureMeasurement" (
        "uuid" TEXT NOT NULL,
        "systolic" INTEGER NOT NULL,
        "diastolic" INTEGER NOT NULL,
        "syncStatus" TEXT NOT NULL,
        "userUuid" TEXT NOT NULL,
        "facilityUuid" TEXT NOT NULL,
        "patientUuid" TEXT NOT NULL,
        "createdAt" TEXT NOT NULL,
        "updatedAt" TEXT NOT NULL,
        "deletedAt" TEXT,
        "recordedAt" TEXT NOT NULL,
        PRIMARY KEY("uuid"))
      """)

      execSQL("""
        INSERT INTO "BloodPressureMeasurement" (
        "uuid", "systolic", "diastolic", "syncStatus", "userUuid", "facilityUuid",
        "patientUuid", "createdAt", "updatedAt","deletedAt","recordedAt"
        )
        SELECT "uuid", "systolic", "diastolic", "syncStatus", "userUuid", "facilityUuid",
        "patientUuid", "createdAt", "updatedAt","deletedAt","recordedAt" FROM "BloodPressureMeasurement_v53"
      """)

      execSQL(""" CREATE INDEX "index_BloodPressureMeasurement_patientUuid" ON "BloodPressureMeasurement" ("patientUuid") """)

      execSQL(""" DROP TABLE "BloodPressureMeasurement_v53" """)

      execSQL(""" DROP TABLE "Encounter" """)
    }
  }
}
