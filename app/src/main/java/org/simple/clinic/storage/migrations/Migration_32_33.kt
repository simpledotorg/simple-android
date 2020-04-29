package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_32_33 @Inject constructor() : Migration(32, 33) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE "Appointment" RENAME TO "Appointment_v32"
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
        "appointmentType" TEXT,
        "syncStatus" TEXT NOT NULL,
        "createdAt" TEXT NOT NULL,
        "updatedAt" TEXT NOT NULL,
        "deletedAt" TEXT,
        PRIMARY KEY("uuid")
      )
    """)

    database.execSQL("""
      INSERT INTO "Appointment"( "uuid", "patientUuid", "facilityUuid", "scheduledDate", "status", "cancelReason", "remindOn",  "agreedToVisit",  "syncStatus",  "createdAt",  "updatedAt",  "deletedAt")
      SELECT "uuid", "patientUuid", "facilityUuid", "scheduledDate", "status", "cancelReason", "remindOn",  "agreedToVisit",  "syncStatus",  "createdAt",  "updatedAt",  "deletedAt" FROM "Appointment_v32"
      """)

    database.execSQL("""DROP TABLE "Appointment_v32" """)
  }
}
