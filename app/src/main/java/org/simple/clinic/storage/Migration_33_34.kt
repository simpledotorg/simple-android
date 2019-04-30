package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration_33_34 : Migration(33, 34) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE "Appointment" RENAME TO "Appointment_v33"
    """)

    database.execSQL("""
      CREATE TABLE IF NOT EXISTS "Appointment" (
        "uuid" TEXT NOT NULL,
        "patientUuid" TEXT NOT NULL,
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

    database.execSQL("""
      INSERT INTO "Appointment"(
        "uuid", "patientUuid", "facilityUuid",
        "scheduledDate", "status", "cancelReason",
        "remindOn",  "agreedToVisit", "appointmentType",
        "syncStatus", "createdAt",  "updatedAt",  "deletedAt"
      )
      SELECT
        "uuid", "patientUuid", "facilityUuid",
        "scheduledDate", "status", "cancelReason",
        "remindOn",  "agreedToVisit", 'manual',
        "syncStatus", "createdAt",  "updatedAt",  "deletedAt"
      FROM "Appointment_v33"
      """
    )
    database.execSQL("""DROP TABLE "Appointment_v33" """)
  }
}
