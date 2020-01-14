package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_55_56 @Inject constructor() : Migration(55, 56) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""ALTER TABLE "Appointment" RENAME TO "Appointment_v55"  """)

      execSQL("""
      CREATE TABLE IF NOT EXISTS "Appointment" (
        "uuid" TEXT NOT NULL,
        "patientUuid" TEXT NOT NULL,
        "creationFacilityUuid" TEXT,
        "facilityUuid" TEXT NOT NULL,
        "scheduledDate" TEXT NOT NULL,
        "status" TEXT NOT NULL,
        "cancelReason" TEXT,
        "remindOn" TEXT,
        "agreedToVisit" INTEGER,
        "appointmentType" TEXT NOT NULL,
        "syncStatus" TEXT NOT NULL,
        "createdAt" TEXT NOT NULL,
        "updatedAt" TEXT NOT NULL,
        "deletedAt" TEXT,
        PRIMARY KEY("uuid")
      )
    """)

      execSQL("""
      INSERT INTO "Appointment"(
        "uuid", "patientUuid", "creationFacilityUuid", "facilityUuid",
        "scheduledDate", "status", "cancelReason",
        "remindOn",  "agreedToVisit", "appointmentType",
        "syncStatus", "createdAt",  "updatedAt",  "deletedAt"
      )
      SELECT
        "uuid", "patientUuid", "facilityUuid", "facilityUuid",
        "scheduledDate", "status", "cancelReason",
        "remindOn",  "agreedToVisit", "appointmentType",
        "syncStatus", "createdAt",  "updatedAt",  "deletedAt"
      FROM "Appointment_v55"
      """
      )
      execSQL("""DROP TABLE "Appointment_v55" """)
    }
  }

}
