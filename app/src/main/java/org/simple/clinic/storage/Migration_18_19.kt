package org.simple.clinic.storage

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

@Suppress("ClassName")
class Migration_18_19 : Migration(18, 19) {
  override fun migrate(database: SupportSQLiteDatabase) {

    database.execSQL("""ALTER TABLE "MedicalHistory" RENAME TO "MedicalHistory_v17" """)
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS "MedicalHistory"
      ("uuid" TEXT NOT NULL,
      "patientUuid" TEXT NOT NULL,
      "diagnosedWithHypertension" TEXT NOT NULL,
      "isOnTreatmentForHypertension" TEXT NOT NULL,
      "hasHadHeartAttack" TEXT NOT NULL,
      "hasHadStroke" TEXT NOT NULL,
      "hasHadKidneyDisease" TEXT NOT NULL,
      "hasDiabetes" TEXT NOT NULL,
      "syncStatus" TEXT NOT NULL,
      "createdAt" TEXT NOT NULL,
      "updatedAt" TEXT NOT NULL,
      PRIMARY KEY("uuid"))
    """)

    database.execSQL("""
    INSERT OR REPLACE INTO "MedicalHistory"("uuid","patientUuid","diagnosedWithHypertension","isOnTreatmentForHypertension","hasHadHeartAttack","hasHadStroke","hasHadKidneyDisease","hasDiabetes","syncStatus","createdAt","updatedAt")
    SELECT "uuid","patientUuid","diagnosedWithHypertension","isOnTreatmentForHypertension","hasHadHeartAttack","hasHadStroke","hasHadKidneyDisease","hasDiabetes","syncStatus","createdAt","updatedAt"
    FROM "MedicalHistory_v17"
    """)

    val columns = arrayOf(
        "diagnosedWithHypertension",
        "isOnTreatmentForHypertension",
        "hasHadHeartAttack",
        "hasHadStroke",
        "hasHadKidneyDisease",
        "hasDiabetes")
    database.inTransaction {
      columns.forEach { column ->
        database.execSQL("""UPDATE "MedicalHistory" SET "$column" = 'YES' WHERE "$column" = '1' """)
        database.execSQL("""UPDATE "MedicalHistory" SET "$column" = 'NO' WHERE "$column" = '0' """)
      }
    }

    database.execSQL("""DROP TABLE "MedicalHistory_v17" """)
  }
}
