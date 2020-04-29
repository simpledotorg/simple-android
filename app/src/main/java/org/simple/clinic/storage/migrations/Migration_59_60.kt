package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_59_60 @Inject constructor() : Migration(59, 60) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        ALTER TABLE "MedicalHistory" RENAME TO "MedicalHistory_v59"
      """)

      execSQL("""
        CREATE TABLE IF NOT EXISTS "MedicalHistory" (
            "uuid" TEXT NOT NULL, "patientUuid" TEXT NOT NULL, 
            "diagnosedWithHypertension" TEXT NOT NULL, "hasHadHeartAttack" TEXT NOT NULL, 
            "hasHadStroke" TEXT NOT NULL, "hasHadKidneyDisease" TEXT NOT NULL, 
            "hasDiabetes" TEXT NOT NULL, "syncStatus" TEXT NOT NULL, 
            "createdAt" TEXT NOT NULL, "updatedAt" TEXT NOT NULL, "deletedAt" TEXT, 
            PRIMARY KEY("uuid")
        )
      """)

      execSQL("""
        INSERT INTO "MedicalHistory" (
            "uuid", "patientUuid", "diagnosedWithHypertension", "hasHadHeartAttack",
            "hasHadStroke", "hasHadKidneyDisease", "hasDiabetes", "syncStatus",
            "createdAt", "updatedAt", "deletedAt"
        )
        SELECT
            "uuid", "patientUuid", "diagnosedWithHypertension", "hasHadHeartAttack",
            "hasHadStroke", "hasHadKidneyDisease", "hasDiabetes", "syncStatus",
            "createdAt", "updatedAt", "deletedAt"
        FROM "MedicalHistory_v59"
      """)

      execSQL(""" DROP TABLE "MedicalHistory_v59" """)
    }
  }
}
