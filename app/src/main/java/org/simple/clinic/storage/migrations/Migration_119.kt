package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_119 @Inject constructor() : Migration(118, 119) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.inTransaction {
      execSQL(""" ALTER TABLE "CVDRisk" RENAME TO "CVDRisk_OLD" """)
      execSQL("""
        CREATE TABLE IF NOT EXISTS "CVDRisk" (
          "uuid" TEXT NOT NULL, 
          "patientUuid" TEXT NOT NULL, 
          "min" INTEGER NOT NULL, 
          "max" INTEGER NOT NULL, 
          "createdAt" TEXT NOT NULL,
          "updatedAt" TEXT NOT NULL,
          "deletedAt" TEXT,
          "syncStatus" TEXT NOT NULL,
          PRIMARY KEY("uuid")
        )
      """)
      execSQL("""
        INSERT INTO "CVDRisk" (
          "uuid",
          "patientUuid",
          "min",
          "max",
          "createdAt",
          "updatedAt",
          "deletedAt",
          "syncStatus"
        )
        SELECT
          "uuid",
          "patientUuid",
          CAST(SUBSTR("riskScore", 1, INSTR("riskScore", '-') - 1) AS INTEGER) AS riskScoreMin,
          CAST(SUBSTR("riskScore", INSTR("riskScore", '-') + 1) AS INTEGER) AS riskScoreMax,
          "createdAt",
          "updatedAt",
          "deletedAt",
          "syncStatus"
        FROM "CVDRisk_OLD"
      """)
      execSQL(""" DROP TABLE "CVDRisk_OLD" """)
      execSQL(""" CREATE INDEX IF NOT EXISTS `index_CVDRisk_patientUuid` ON `CVDRisk` (`patientUuid`) """)
    }
  }
}
