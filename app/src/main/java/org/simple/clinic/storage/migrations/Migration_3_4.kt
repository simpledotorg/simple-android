package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_3_4 @Inject constructor() : Migration(3, 4) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS `LoggedInUser` (
        `uuid` TEXT NOT NULL,
        `fullName` TEXT NOT NULL,
        `phoneNumber` TEXT NOT NULL,
        `pinDigest` TEXT NOT NULL,
        `facilityUuid` TEXT NOT NULL,
        `status` TEXT NOT NULL,
        `createdAt` TEXT NOT NULL,
        `updatedAt` TEXT NOT NULL,
        PRIMARY KEY(`uuid`)
      )
      """)
  }
}
