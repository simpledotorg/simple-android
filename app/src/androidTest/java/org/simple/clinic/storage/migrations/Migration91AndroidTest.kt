package org.simple.clinic.storage.migrations

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration91AndroidTest : BaseDatabaseMigrationTest(
    fromVersion = 90,
    toVersion = 91
) {

  @Test
  fun the_retain_until_column_should_be_added_to_patients_table() {
    val patientId = UUID.fromString("939edc81-da26-49ae-a907-e1c5a5aa9d12")
    val patientAddressId = UUID.fromString("aa6b5df2-55ed-45a1-8be3-3ef6abca8593")

    before.insert("PatientAddress", mapOf(
        "uuid" to patientAddressId,
        "streetAddress" to "Richmond Road",
        "colonyOrVillage" to "Central Bangalore",
        "zone" to null,
        "district" to "Bangalore Urban",
        "state" to "Karnataka",
        "country" to "India",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "deletedAt" to null
    ))

    before.insert("Patient", mapOf(
        "uuid" to patientId,
        "addressUuid" to patientAddressId,
        "fullName" to "Anish Acharya",
        "gender" to "male",
        "dateOfBirth" to "1942-04-01",
        "age_value" to null,
        "age_updatedAt" to null,
        "status" to "active",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
        "deletedAt" to Instant.parse("2018-01-01T00:00:03Z"),
        "recordedAt" to Instant.parse("2018-01-01T00:00:02Z"),
        "syncStatus" to "DONE",
        "reminderConsent" to "denied",
        "deletedReason" to "accidental_registration",
        "registeredFacilityId" to UUID.fromString("f6566aea-53fa-4c5b-883a-9eb2457ca4d9"),
        "assignedFacilityId" to UUID.fromString("b61a1cbd-e31e-4fdc-860a-dcd08b22693c")
    ))

    after.query("""
      SELECT * FROM "Patient" WHERE "uuid" = '$patientId'
    """.trimIndent()).use { cursor ->
      assertThat(cursor.count).isEqualTo(1)
      assertThat(cursor.moveToFirst()).isTrue()

      cursor.assertValues(mapOf(
          "uuid" to patientId,
          "addressUuid" to patientAddressId,
          "fullName" to "Anish Acharya",
          "gender" to "male",
          "dateOfBirth" to "1942-04-01",
          "age_value" to null,
          "age_updatedAt" to null,
          "status" to "active",
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
          "deletedAt" to Instant.parse("2018-01-01T00:00:03Z"),
          "recordedAt" to Instant.parse("2018-01-01T00:00:02Z"),
          "syncStatus" to "DONE",
          "reminderConsent" to "denied",
          "deletedReason" to "accidental_registration",
          "registeredFacilityId" to UUID.fromString("f6566aea-53fa-4c5b-883a-9eb2457ca4d9"),
          "assignedFacilityId" to UUID.fromString("b61a1cbd-e31e-4fdc-860a-dcd08b22693c"),
          "retainUntil" to null
      ))
    }
  }
}
