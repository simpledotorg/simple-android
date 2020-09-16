package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("Classname")
class Migration_79 @Inject constructor() : Migration(78, 79) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        ALTER TABLE "TeleconsultRecord" ADD COLUMN "syncStatus" TEXT NOT NULL DEFAULT 'PENDING'
        """)
    }
  }
}
