package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_121 @Inject constructor() : Migration(120, 121) {

  override fun migrate(db: SupportSQLiteDatabase) {
    db.inTransaction {
      execSQL("""
        ALTER TABLE "MedicalHistory"
        ADD COLUMN "isUsingSmokelessTobacco" TEXT NOT NULL DEFAULT "unknown"
    """.trimIndent())
    }
  }
}
