package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_116 @Inject constructor() : Migration(115, 116) {

  override fun migrate(db: SupportSQLiteDatabase) {
    db.inTransaction {
      execSQL("""
        ALTER TABLE "MedicalHistory"
        ADD COLUMN "smoking" TEXT NOT NULL DEFAULT "unknown"
    """.trimIndent())
    }
  }
}
