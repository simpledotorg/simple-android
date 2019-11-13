package org.simple.clinic.storage.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Suppress("ClassName")
class Migration_24_25 @javax.inject.Inject constructor() : Migration(24, 25) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE "Facility" ADD COLUMN "protocolUuid" TEXT DEFAULT null
    """)
  }
}
