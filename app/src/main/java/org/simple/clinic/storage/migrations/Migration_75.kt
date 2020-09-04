package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

class Migration_75 @Inject constructor() : Migration(74, 75) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        ALTER TABLE "LoggedInUser" ADD COLUMN "capability_canTeleconsult" TEXT DEFAULT NULL
      """)

      execSQL("""
        ALTER TABLE "OngoingLoginEntry" ADD COLUMN "capability_canTeleconsult" TEXT DEFAULT NULL
      """)

    }
  }
}
