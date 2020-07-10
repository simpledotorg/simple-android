package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration60AndroidTest : BaseDatabaseMigrationTest(59, 60) {

  @Test
  fun migrating_to_60_should_remove_the_isOnTreatmentForHypertension_column_from_MedicalHistory() {
    before.assertColumns("MedicalHistory", setOf(
        "uuid",
        "patientUuid",
        "isOnTreatmentForHypertension",
        "diagnosedWithHypertension",
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
  fun migrating_to_60_should_retain_the_existing_answers() {
    val firstMedicalHistoryUuid = UUID.fromString("d88283c5-5506-4642-a2c9-7cd5c8a868b4")
    val secondMedicalHistoryUuid = UUID.fromString("c68bfa06-e3cd-4efa-847d-17c390063f27")

    before.insert("MedicalHistory", mapOf(
        "uuid" to firstMedicalHistoryUuid,
        "patientUuid" to UUID.fromString("b79f4f13-cd96-41cc-be0f-ce8403269e8e"),
        "isOnTreatmentForHypertension" to "yes",
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
    before.insert("MedicalHistory", mapOf(
        "uuid" to secondMedicalHistoryUuid,
        "patientUuid" to UUID.fromString("ccd6c8e1-f3a7-4472-ada1-92068e87e4f1"),
        "isOnTreatmentForHypertension" to "no",
        "diagnosedWithHypertension" to "yes",
        "hasHadHeartAttack" to "unknown",
        "hasHadStroke" to "yes",
        "hasHadKidneyDisease" to "no",
        "hasDiabetes" to "yes",
        "syncStatus" to "PENDING",
        "createdAt" to Instant.parse("2018-01-02T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-02T00:00:01Z"),
        "deletedAt" to Instant.parse("2018-01-02T00:00:02Z")
    ))

    after
        .query(""" SELECT * FROM "MedicalHistory" WHERE "uuid" = '$firstMedicalHistoryUuid' """)
        .use { cursor ->
          cursor.moveToFirst()
          cursor.assertValues(mapOf(
              "uuid" to firstMedicalHistoryUuid,
              "patientUuid" to UUID.fromString("b79f4f13-cd96-41cc-be0f-ce8403269e8e"),
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
    after
        .query(""" SELECT * FROM "MedicalHistory" WHERE "uuid" = '$secondMedicalHistoryUuid' """)
        .use { cursor ->
          cursor.moveToFirst()
          cursor.assertValues(mapOf(
              "uuid" to secondMedicalHistoryUuid,
              "patientUuid" to UUID.fromString("ccd6c8e1-f3a7-4472-ada1-92068e87e4f1"),
              "diagnosedWithHypertension" to "yes",
              "hasHadHeartAttack" to "unknown",
              "hasHadStroke" to "yes",
              "hasHadKidneyDisease" to "no",
              "hasDiabetes" to "yes",
              "syncStatus" to "PENDING",
              "createdAt" to Instant.parse("2018-01-02T00:00:00Z"),
              "updatedAt" to Instant.parse("2018-01-02T00:00:01Z"),
              "deletedAt" to Instant.parse("2018-01-02T00:00:02Z")
          ))
        }
  }
}
