package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_123 @Inject constructor() : Migration(122, 123) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.inTransaction {
      execSQL("""
        CREATE TABLE IF NOT EXISTS "ReturnScore" (
          "uuid" TEXT NOT NULL, 
          "patientUuid" TEXT NOT NULL, 
          "scoreType" TEXT NOT NULL, 
          "scoreValue" REAL NOT NULL, 
          "createdAt" TEXT NOT NULL,
          "updatedAt" TEXT NOT NULL,
          "deletedAt" TEXT,
          PRIMARY KEY("uuid")
        )
      """)

      execSQL("CREATE INDEX IF NOT EXISTS `index_ReturnScore_patientUuid` ON `ReturnScore` (`patientUuid`)")
    }
  }
}
