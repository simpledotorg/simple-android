package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration_48_49 : Migration(48, 49) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL(""" 
      CREATE TABLE IF NOT EXISTS "Encounter" (
      "uuid" TEXT NOT NULL,
      "patientUuid" TEXT NOT NULL,
      "encounteredOn" TEXT NOT NULL,
      "createdAt" TEXT NOT NULL,
      "updatedAt" TEXT NOT NULL,
      "deletedAt" TEXT,
      PRIMARY KEY("uuid")
      )
      """)
  }
}
