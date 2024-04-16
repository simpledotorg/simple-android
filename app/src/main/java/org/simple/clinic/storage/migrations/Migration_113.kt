package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_113 @Inject constructor() : Migration(112, 113) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        ALTER TABLE "Patient" ADD COLUMN "isEligibleForReassignment" INTEGER NOT NULL DEFAULT 0
      """)
    }
  }
}
