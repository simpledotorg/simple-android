package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration97AndroidTest : BaseDatabaseMigrationTest(
    fromVersion = 96,
    toVersion = 97
) {

  @Test
  fun migration_should_add_is_on_diabetes_treatment_column_to_medical_history_table() {
    before.assertColumns("MedicalHistory", setOf(
        "uuid",
        "patientUuid",
        "diagnosedWithHypertension",
        "isOnHypertensionTreatment",
        "hasHadHeartAttack",
        "hasHadStroke",
        "hasHadKidneyDisease",
        "hasDiabetes",
        "syncStatus",
        "createdAt",
        "updatedAt",
        "deletedAt"
    ))

    after.assertColumns("MedicalHistory", setOf(
        "uuid",
        "patientUuid",
        "diagnosedWithHypertension",
        "isOnHypertensionTreatment",
        "isOnDiabetesTreatment",
        "hasHadHeartAttack",
        "hasHadStroke",
        "hasHadKidneyDisease",
        "hasDiabetes",
        "syncStatus",
        "createdAt",
        "updatedAt",
        "deletedAt"
    ))
  }

  @Test
  fun migrating_to_97_should_retain_the_existing_answers_and_add_default_diabetes_treatment_answer() {
    val medicalHistoryUuid = UUID.fromString("d88283c5-5506-4642-a2c9-7cd5c8a868b4")
    val patientUuid = UUID.fromString("b79f4f13-cd96-41cc-be0f-ce8403269e8e")

    before.insert("MedicalHistory", mapOf(
        "uuid" to medicalHistoryUuid,
        "patientUuid" to patientUuid,
        "isOnHypertensionTreatment" to "yes",
        "diagnosedWithHypertension" to "unknown",
        "hasHadHeartAttack" to "no",
        "hasHadStroke" to "unknown",
        "hasHadKidneyDisease" to "yes",
        "hasDiabetes" to "no",
        "syncStatus" to "DONE",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
        "deletedAt" to Instant.parse("2018-01-01T00:00:02Z")
    ))

    after
        .query(""" SELECT * FROM "MedicalHistory" WHERE "uuid" = '$medicalHistoryUuid' """)
        .use { cursor ->
          cursor.moveToFirst()
          cursor.assertValues(mapOf(
              "uuid" to medicalHistoryUuid,
              "patientUuid" to patientUuid,
              "isOnHypertensionTreatment" to "yes",
              "isOnDiabetesTreatment" to "unknown",
              "diagnosedWithHypertension" to "unknown",
              "hasHadHeartAttack" to "no",
              "hasHadStroke" to "unknown",
              "hasHadKidneyDisease" to "yes",
              "hasDiabetes" to "no",
              "syncStatus" to "DONE",
              "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
              "deletedAt" to Instant.parse("2018-01-01T00:00:02Z")
          ))
        }
  }
}
