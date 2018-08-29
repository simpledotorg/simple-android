package org.simple.clinic.storage

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

@Suppress("ClassName")
class Migration_3_4 : Migration(3, 4) {

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
