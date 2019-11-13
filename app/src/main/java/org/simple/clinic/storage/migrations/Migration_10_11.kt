package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.overdue.Appointment
import javax.inject.Inject

/**
 * Renames 'id' -> 'uuid' in [Appointment] and [Communication]
 */
@Suppress("ClassName")
class Migration_10_11 @Inject constructor() : Migration(10, 11) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("ALTER TABLE `Appointment` RENAME TO `Appointment_v10`")
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS `Appointment` (
          `uuid` TEXT NOT NULL,
          `patientUuid` TEXT NOT NULL,
          `facilityUuid` TEXT NOT NULL,
          `date` TEXT NOT NULL,
          `status` TEXT NOT NULL,
          `statusReason` TEXT NOT NULL,
          `syncStatus` TEXT NOT NULL,
          `createdAt` TEXT NOT NULL,
          `updatedAt` TEXT NOT NULL,
          PRIMARY KEY(`uuid`))
      """)
    database.execSQL("""
      INSERT INTO `Appointment`(`uuid`, `patientUuid`, `facilityUuid`, `date`, `status`, `statusReason`, `syncStatus`, `createdAt`, `updatedAt`)
      SELECT `id`, `patientId`, `facilityId`, `date`, `status`, `statusReason`, `syncStatus`, `createdAt`, `updatedAt`
      FROM `Appointment_v10`
      """)
    database.execSQL("DROP TABLE `Appointment_v10`")

    database.execSQL("ALTER TABLE `Communication` RENAME TO `Communication_v10`")
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS `Communication` (
          `uuid` TEXT NOT NULL,
          `appointmentUuid` TEXT NOT NULL,
          `userUuid` TEXT NOT NULL,
          `type` TEXT NOT NULL,
          `result` TEXT NOT NULL,
          `syncStatus` TEXT NOT NULL,
          `createdAt` TEXT NOT NULL,
          `updatedAt` TEXT NOT NULL,
          PRIMARY KEY(`uuid`))
      """)
    database.execSQL("""
      INSERT INTO `Communication`(`uuid`, `appointmentUuid`, `userUuid`, `type`, `result`, `syncStatus`, `createdAt`, `updatedAt`)
      SELECT `id`, `appointmentId`, `userId`, `type`, `result`, `syncStatus`, `createdAt`, `updatedAt`
      FROM `Communication_v10`
      """)
    database.execSQL("DROP TABLE `Communication_v10`")
  }
}
