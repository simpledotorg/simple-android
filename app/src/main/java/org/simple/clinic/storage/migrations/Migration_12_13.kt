package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

/**
 * Updates the [Appointment] and [Communication] models
 */
@Suppress("ClassName")
class Migration_12_13 @Inject constructor() : Migration(12, 13) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("ALTER TABLE `Appointment` RENAME TO `Appointment_v12`")
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS `Appointment` (
          `uuid` TEXT NOT NULL,
          `patientUuid` TEXT NOT NULL,
          `facilityUuid` TEXT NOT NULL,
          `scheduledDate` TEXT NOT NULL,
          `status` TEXT NOT NULL,
          `cancelReason` TEXT,
          `remindOn` TEXT,
          `agreedToVisit` INTEGER,
          `syncStatus` TEXT NOT NULL,
          `createdAt` TEXT NOT NULL,
          `updatedAt` TEXT NOT NULL,
          PRIMARY KEY(`uuid`))
      """)
    database.execSQL("""
      INSERT INTO `Appointment`(`uuid`, `patientUuid`, `facilityUuid`, `scheduledDate`, `status`, `cancelReason`, `syncStatus`, `createdAt`, `updatedAt`)
      SELECT `uuid`, `patientUuid`, `facilityUuid`, `date`, `status`, `statusReason`, `syncStatus`, `createdAt`, `updatedAt`
      FROM `Appointment_v12`
      """)
    database.execSQL("""
      UPDATE `Appointment` SET `cancelReason` = null WHERE `cancelReason` = "NOT_CALLED_YET"
    """)
    database.execSQL("DROP TABLE `Appointment_v12`")
  }
}
