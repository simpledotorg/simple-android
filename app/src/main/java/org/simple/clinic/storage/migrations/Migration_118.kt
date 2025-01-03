package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_118 @Inject constructor() : Migration(117, 118) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.inTransaction {
      execSQL("""
        CREATE TABLE IF NOT EXISTS "CVDRisk" (
          "uuid" TEXT NOT NULL, 
          "patientUuid" TEXT NOT NULL, 
          "riskScore" TEXT NOT NULL, 
          "createdAt" TEXT NOT NULL,
          "updatedAt" TEXT NOT NULL,
          "deletedAt" TEXT,
          "syncStatus" TEXT NOT NULL,
          PRIMARY KEY("uuid")
        )
      """)

      execSQL("CREATE INDEX IF NOT EXISTS `index_CVDRisk_patientUuid` ON `CVDRisk` (`patientUuid`)")
    }
  }
}
