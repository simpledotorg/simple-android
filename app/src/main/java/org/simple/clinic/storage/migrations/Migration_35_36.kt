package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_35_36 @Inject constructor() : Migration(35, 36) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("PRAGMA foreign_keys = OFF")
    database.inTransaction {
      migratePrescribedDrugs(database)
      migrateAppointments(database)
      migrateMedicalHistories(database)
      migratePatientAddress(database)
      migratePatientPhoneNumber(database)
      migrateBusinessIds(database)
      database.execSQL("PRAGMA foreign_key_check")
    }
    database.execSQL("PRAGMA foreign_keys = ON")
  }

  private fun migratePrescribedDrugs(database: SupportSQLiteDatabase) {
    database.execSQL("""ALTER TABLE "PrescribedDrug" RENAME TO "PrescribedDrug_V35" """)
    database.execSQL("""CREATE TABLE IF NOT EXISTS "PrescribedDrug" (
      "uuid" TEXT NOT NULL,
      "name" TEXT NOT NULL,
      "dosage" TEXT,
      "rxNormCode" TEXT,
      "isDeleted" INTEGER NOT NULL,
      "isProtocolDrug" INTEGER NOT NULL,
      "patientUuid" TEXT NOT NULL,
      "facilityUuid" TEXT NOT NULL,
      "syncStatus" TEXT NOT NULL,
      "createdAt" TEXT NOT NULL,
      "updatedAt" TEXT NOT NULL,
      "deletedAt" TEXT,
      PRIMARY KEY("uuid"))
    """)

    database.execSQL("""
      INSERT INTO "PrescribedDrug"("uuid", "name", "dosage", "rxNormCode", "isDeleted", "isProtocolDrug", "patientUuid", "facilityUuid",
        "syncStatus", "createdAt", "updatedAt", "deletedAt")
      SELECT "uuid", "name", "dosage", "rxNormCode", "isDeleted", "isProtocolDrug", "patientUuid", "facilityUuid", "syncStatus",
        "createdAt", "updatedAt", "deletedAt"
      FROM "PrescribedDrug_V35"
    """)

    database.execSQL("""DROP TABLE "PrescribedDrug_V35"""")
    database.execSQL("""CREATE INDEX "index_PrescribedDrug_patientUuid" ON "PrescribedDrug" ("patientUuid")""")
  }

  private fun migrateAppointments(database: SupportSQLiteDatabase) {
    database.execSQL("""
      ALTER TABLE "Appointment" RENAME TO "Appointment_v35"
    """)

    database.execSQL("""
      CREATE TABLE IF NOT EXISTS "Appointment" (
        "uuid" TEXT NOT NULL,
        "patientUuid" TEXT NOT NULL,
        "facilityUuid" TEXT NOT NULL,
        "scheduledDate" TEXT NOT NULL,
        "status" TEXT NOT NULL,
        "cancelReason" TEXT,
        "remindOn" TEXT,
        "agreedToVisit" INTEGER,
        "appointmentType" TEXT NOT NULL,
        "syncStatus" TEXT NOT NULL,
        "createdAt" TEXT NOT NULL,
        "updatedAt" TEXT NOT NULL,
        "deletedAt" TEXT,
        PRIMARY KEY("uuid")
      )
    """)

    database.execSQL("""
      INSERT INTO "Appointment"(
        "uuid", "patientUuid", "facilityUuid",
        "scheduledDate", "status", "cancelReason",
        "remindOn",  "agreedToVisit", "appointmentType",
        "syncStatus", "createdAt",  "updatedAt",  "deletedAt"
      )
      SELECT
        "uuid", "patientUuid", "facilityUuid",
        "scheduledDate", "status", "cancelReason",
        "remindOn",  "agreedToVisit", "appointmentType",
        "syncStatus", "createdAt",  "updatedAt",  "deletedAt"
      FROM "Appointment_v35"
      """
    )
    database.execSQL("""DROP TABLE "Appointment_v35" """)
  }

  private fun migrateMedicalHistories(database: SupportSQLiteDatabase) {
    database.execSQL("""ALTER TABLE "MedicalHistory" RENAME TO "MedicalHistory_V35" """)
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
      "deletedAt" TEXT,
      PRIMARY KEY("uuid"))
    """)

    database.execSQL("""
    INSERT OR REPLACE INTO "MedicalHistory"("uuid","patientUuid","diagnosedWithHypertension","isOnTreatmentForHypertension","hasHadHeartAttack","hasHadStroke","hasHadKidneyDisease","hasDiabetes","syncStatus","createdAt","updatedAt", "deletedAt")
    SELECT "uuid","patientUuid","diagnosedWithHypertension","isOnTreatmentForHypertension","hasHadHeartAttack","hasHadStroke","hasHadKidneyDisease","hasDiabetes","syncStatus","createdAt","updatedAt", "deletedAt"
    FROM "MedicalHistory_V35"
    """)

    database.execSQL("""DROP TABLE "MedicalHistory_V35" """)
  }

  private fun migratePatientAddress(database: SupportSQLiteDatabase) {
    database.execSQL(""" PRAGMA legacy_alter_table = ON """)
    database.execSQL(""" DROP INDEX "index_Patient_addressUuid" """)
    database.execSQL("""ALTER TABLE "PatientAddress" RENAME TO "PatientAddress_V35" """)
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS "PatientAddress" (
      "uuid" TEXT NOT NULL,
      "colonyOrVillage" TEXT,
      "district" TEXT NOT NULL,
      "state" TEXT NOT NULL,
      "country" TEXT,
      "createdAt" TEXT NOT NULL,
      "updatedAt" TEXT NOT NULL,
      "deletedAt" TEXT,
      PRIMARY KEY("uuid")
      )
    """)

    database.execSQL("""
      INSERT OR REPLACE INTO "PatientAddress"("uuid", "colonyOrVillage","district", "state", "country", "createdAt", "updatedAt", "deletedAt")
      SELECT "uuid", "colonyOrVillage","district", "state", "country", "createdAt", "updatedAt", "deletedAt"
      FROM "PatientAddress_V35"
    """)

    database.execSQL("""DROP TABLE "PatientAddress_V35" """)

    database.execSQL(""" CREATE INDEX "index_Patient_addressUuid" ON "Patient" ("addressUuid") """)
    database.execSQL(""" PRAGMA legacy_alter_table = OFF """)
  }

  private fun migratePatientPhoneNumber(database: SupportSQLiteDatabase) {
    database.execSQL("""ALTER TABLE "PatientPhoneNumber" RENAME TO "PatientPhoneNumber_V35" """)
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS "PatientPhoneNumber" (
      "uuid" TEXT NOT NULL,
      "patientUuid" TEXT NOT NULL,
      "number" TEXT NOT NULL,
      "phoneType" TEXT NOT NULL,
      "active" INTEGER NOT NULL,
      "createdAt" TEXT NOT NULL,
      "updatedAt" TEXT NOT NULL,
      "deletedAt" TEXT,
      PRIMARY KEY("uuid"),
      FOREIGN KEY("patientUuid") REFERENCES "Patient"("uuid") ON UPDATE CASCADE ON DELETE CASCADE
      )
    """)

    database.execSQL("""
      INSERT OR REPLACE INTO "PatientPhoneNumber" ("uuid", "patientUuid", "number", "phoneType", "active", "createdAt", "updatedAt", "deletedAt")
      SELECT "uuid", "patientUuid", "number", "phoneType", "active", "createdAt", "updatedAt", "deletedAt"
      FROM "PatientPhoneNumber_V35"
    """)

    database.execSQL("""DROP TABLE "PatientPhoneNumber_V35" """)
    database.execSQL("""
        CREATE INDEX "index_PatientPhoneNumber_patientUuid" ON "PatientPhoneNumber" ("patientUuid")
      """)
  }

  private fun migrateBusinessIds(database: SupportSQLiteDatabase) {
    database.execSQL("""ALTER TABLE "BusinessId" RENAME TO "BusinessId_V35" """)
    database.execSQL("""
        CREATE TABLE IF NOT EXISTS "BusinessId" (
          "uuid" TEXT NOT NULL,
          "patientUuid" TEXT NOT NULL,
          "identifier" TEXT NOT NULL,
          "identifierType" TEXT NOT NULL,
          "metaVersion" TEXT NOT NULL,
          "meta" TEXT NOT NULL,
          "createdAt" TEXT NOT NULL,
          "updatedAt" TEXT NOT NULL,
          "deletedAt" TEXT,
          PRIMARY KEY("uuid"),
          FOREIGN KEY("patientUuid") REFERENCES "Patient"("uuid") ON UPDATE NO ACTION ON DELETE CASCADE)
        """)

    database.execSQL("""
      INSERT OR REPLACE INTO "BusinessId" ("uuid", "patientUuid", "identifier", "identifierType", "metaVersion", "meta", "createdAt", "updatedAt", "deletedAt")
      SELECT "uuid", "patientUuid", "identifier", "identifierType", "metaVersion", "meta", "createdAt", "updatedAt", "deletedAt"
      FROM "BusinessId_V35"
    """)

    database.execSQL("""DROP TABLE "BusinessId_V35" """)

    database.execSQL("""
        CREATE INDEX "index_BusinessId_patientUuid" ON "BusinessId" ("patientUuid")
      """)
    database.execSQL("""
        CREATE INDEX "index_BusinessId_identifier" ON "BusinessId" ("identifier")
      """)
  }
}
