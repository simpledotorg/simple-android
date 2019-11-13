package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_18_19 @Inject constructor() : Migration(18, 19) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""CREATE TABLE IF NOT EXISTS "OngoingLoginEntry" (
      "uuid" TEXT NOT NULL,
      "phoneNumber" TEXT NOT NULL,
      "pin" TEXT,
      PRIMARY KEY("uuid"))""")
  }
}
