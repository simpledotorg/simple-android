package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_8_9 @Inject constructor() : Migration(8, 9) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS `Appointment` (
          `id` TEXT NOT NULL,
          `patientId` TEXT NOT NULL,
          `facilityId` TEXT NOT NULL,
          `date` TEXT NOT NULL,
          `status` TEXT NOT NULL,
          `statusReason` TEXT NOT NULL,
          `syncStatus` TEXT NOT NULL,
          `createdAt` TEXT NOT NULL,
          `updatedAt` TEXT NOT NULL,
          PRIMARY KEY(`id`))
    """)
  }
}
