package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("Classname")
class Migration_77 @Inject constructor() : Migration(76, 77) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        ALTER TABLE "PrescribedDrug" ADD COLUMN "teleconsultationId" TEXT DEFAULT NULL
      """)
    }
  }
}
