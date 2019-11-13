package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_25_26 @Inject constructor() : Migration(25, 26) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE ProtocolDrug ADD COLUMN "order" INTEGER NOT NULL DEFAULT 0
    """)
  }
}
