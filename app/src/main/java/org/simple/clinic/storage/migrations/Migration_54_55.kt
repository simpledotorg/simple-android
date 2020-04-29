package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_54_55 @Inject constructor() : Migration(54, 55) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        CREATE TABLE IF NOT EXISTS "BloodSugarMeasurements" (
          "uuid" TEXT NOT NULL, "recordedAt" TEXT NOT NULL, "patientUuid" TEXT NOT NULL, 
          "userUuid" TEXT NOT NULL, "facilityUuid" TEXT NOT NULL, "syncStatus" TEXT NOT NULL, 
          "reading_value" INTEGER NOT NULL, "reading_type" TEXT NOT NULL, 
          "createdAt" TEXT NOT NULL, "updatedAt" TEXT NOT NULL, "deletedAt" TEXT, 
          PRIMARY KEY("uuid")
        )
      """)
    }
  }
}
