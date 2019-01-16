package org.simple.clinic.storage

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Suppress("ClassName")
class Migration_24_25 : Migration(24, 25) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE "Facility" ADD COLUMN "protocolUuid" TEXT DEFAULT null
    """)
  }
}
