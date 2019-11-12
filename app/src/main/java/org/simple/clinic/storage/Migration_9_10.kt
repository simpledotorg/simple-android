package org.simple.clinic.storage

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Suppress("ClassName")
class Migration_9_10 @javax.inject.Inject constructor() : Migration(9, 10) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS `Communication` (
        `id` TEXT NOT NULL,
        `appointmentId` TEXT NOT NULL,
        `userId` TEXT NOT NULL,
        `type` TEXT NOT NULL,
        `result` TEXT NOT NULL,
        `syncStatus` TEXT NOT NULL,
        `createdAt` TEXT NOT NULL,
        `updatedAt` TEXT NOT NULL,
        PRIMARY KEY(`id`))
    """)
  }
}
