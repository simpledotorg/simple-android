package org.simple.clinic.storage.migrations

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration94AndroidTest : BaseDatabaseMigrationTest(
    fromVersion = 93,
    toVersion = 94
) {

  @Test
  fun should_remove_all_the_prescribed_drugs_that_have_an_empty_name() {
    val prescribedDrugWithName = UUID.fromString("a425fb35-8848-4f31-bfcc-bf477bc23dd0")
    val prescribedDrugWithOutName = UUID.fromString("a99b3826-ce86-4ab9-9870-62012cc0a557")
    val deletedPrescribedDrugWithoutName = UUID.fromString("95385906-2946-466d-9d5d-94bea4e71b07")

    before.insert("PrescribedDrug", mapOf(
        "uuid" to prescribedDrugWithName,
        "name" to "Amlodipine",
        "dosage" to null,
        "rxNormCode" to null,
        "isDeleted" to false,
        "isProtocolDrug" to false,
        "patientUuid" to UUID.fromString("74db05a3-dce4-47ed-80de-cc0a96f118fa"),
        "facilityUuid" to UUID.fromString("2b74664f-b071-40ec-adcc-157fa2cf8a5e"),
        "syncStatus" to "PENDING",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
        "deletedAt" to null,
        "frequency" to "OD",
        "durationInDays" to null,
        "teleconsultationId" to null
    ))

    before.insert("PrescribedDrug", mapOf(
        "uuid" to prescribedDrugWithOutName,
        "name" to "",
        "dosage" to null,
        "rxNormCode" to null,
        "isDeleted" to false,
        "isProtocolDrug" to false,
        "patientUuid" to UUID.fromString("f7b6f704-e617-47e7-90f5-1563b8ac8756"),
        "facilityUuid" to UUID.fromString("41c5ee5a-f553-440b-9e82-8ac3d98265b6"),
        "syncStatus" to "PENDING",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
        "deletedAt" to null,
        "frequency" to "BD",
        "durationInDays" to null,
        "teleconsultationId" to null
    ))

    before.insert("PrescribedDrug", mapOf(
        "uuid" to deletedPrescribedDrugWithoutName,
        "name" to "",
        "dosage" to null,
        "rxNormCode" to null,
        "isDeleted" to true,
        "isProtocolDrug" to false,
        "patientUuid" to UUID.fromString("f7b6f704-e617-47e7-90f5-1563b8ac8756"),
        "facilityUuid" to UUID.fromString("41c5ee5a-f553-440b-9e82-8ac3d98265b6"),
        "syncStatus" to "PENDING",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
        "deletedAt" to null,
        "frequency" to "BD",
        "durationInDays" to null,
        "teleconsultationId" to null
    ))

    after.query(""" SELECT * FROM PrescribedDrug """).use { cursor ->
      assertThat(cursor.count).isEqualTo(1)
      assertThat(cursor.moveToFirst()).isTrue()

      cursor.assertValues(mapOf(
          "uuid" to prescribedDrugWithName,
          "name" to "Amlodipine",
          "dosage" to null,
          "rxNormCode" to null,
          "isDeleted" to false,
          "isProtocolDrug" to false,
          "patientUuid" to UUID.fromString("74db05a3-dce4-47ed-80de-cc0a96f118fa"),
          "facilityUuid" to UUID.fromString("2b74664f-b071-40ec-adcc-157fa2cf8a5e"),
          "syncStatus" to "PENDING",
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
          "deletedAt" to null,
          "frequency" to "OD",
          "durationInDays" to null,
          "teleconsultationId" to null
      ))
    }
  }
}
