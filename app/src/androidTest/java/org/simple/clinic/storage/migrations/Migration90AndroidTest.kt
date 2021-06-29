package org.simple.clinic.storage.migrations

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration90AndroidTest : BaseDatabaseMigrationTest(89, 90) {

  @Test
  fun is_on_hypertension_treatment_column_should_be_added_to_medical_history_table() {
    val patientId = UUID.fromString("3b6ac934-e8cd-4411-9622-4070429b9182")
    val medicalHistoryId = UUID.fromString("d69a3bba-129c-4b7c-8279-318792d5b06e")

    before.insert("MedicalHistory", mapOf(
        "uuid" to medicalHistoryId,
        "patientUuid" to patientId,
        "diagnosedWithHypertension" to "yes",
        "hasHadHeartAttack" to "no",
        "hasHadStroke" to "no",
        "hasHadKidneyDisease" to "no",
        "hasDiabetes" to "no",
        "syncStatus" to "DONE",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "deletedAt" to null
    ))

    after.query("""
      SELECT * FROM "MedicalHistory" WHERE "uuid" = '$medicalHistoryId'
    """.trimIndent()).use { cursor ->
      assertThat(cursor.count).isEqualTo(1)
      assertThat(cursor.moveToNext()).isTrue()

      cursor.assertValues(mapOf(
          "uuid" to medicalHistoryId,
          "patientUuid" to patientId,
          "diagnosedWithHypertension" to "yes",
          "isOnHypertensionTreatment" to "unknown",
          "hasHadHeartAttack" to "no",
          "hasHadStroke" to "no",
          "hasHadKidneyDisease" to "no",
          "hasDiabetes" to "no",
          "syncStatus" to "DONE",
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "deletedAt" to null
      ))
    }
  }
}
