package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_122 @Inject constructor() : Migration(121, 122) {

  override fun migrate(db: SupportSQLiteDatabase) {
    db.inTransaction {
      execSQL("""
        ALTER TABLE "MedicalHistory"
        ADD COLUMN "hypertensionDiagnosedAt" TEXT
    """.trimIndent())

      execSQL("""
        ALTER TABLE "MedicalHistory"
        ADD COLUMN "diabetesDiagnosedAt" TEXT
    """.trimIndent())
    }
  }
}
