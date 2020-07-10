package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import org.simple.clinic.patient.SyncStatus
import java.time.Instant
import java.util.UUID

class Migration67AndroidTest : BaseDatabaseMigrationTest(66, 67) {

  @Test
  fun deleted_reason_should_be_added_to_patient_table() {
    before.assertColumns("Patient", setOf(
        "uuid",
        "addressUuid",
        "fullName",
        "gender",
        "dateOfBirth",
        "age_value",
        "age_updatedAt",
        "status",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "recordedAt",
        "syncStatus",
        "reminderConsent"
    ))

    after.assertColumns("Patient", setOf(
        "uuid",
        "addressUuid",
        "fullName",
        "gender",
        "dateOfBirth",
        "age_value",
        "age_updatedAt",
        "status",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "recordedAt",
        "syncStatus",
        "reminderConsent",
        "deletedReason"
    ))
  }

  @Test
  fun patient_record_is_retained_after_the_migration() {
    val addressUuid = UUID.fromString("4f6918ea-d427-4314-bd9e-dfae7741a733")

    before.insert("PatientAddress", mapOf(
        "uuid" to addressUuid,
        "streetAddress" to "12/70 M.G Road",
        "colonyOrVillage" to null,
        "zone" to null,
        "district" to "Bathinda",
        "state" to "Punjab",
        "country" to "India",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "deletedAt" to null
    ))
    before.insert("Patient", mapOf(
        "uuid" to UUID.fromString("c187a3cc-3562-476a-93d0-9ad263ccbd9e"),
        "addressUuid" to addressUuid,
        "fullName" to "Jane Doe",
        "gender" to "female",
        "dateOfBirth" to null,
        "age_value" to 29,
        "age_updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "status" to "active",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "deletedAt" to null,
        "recordedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "syncStatus" to SyncStatus.DONE.name,
        "reminderConsent" to "granted"
    ))

    after.query(""" SELECT * FROM Patient """).use { cursor ->
      cursor.moveToNext()
      cursor.assertValues(mapOf(
          "uuid" to UUID.fromString("c187a3cc-3562-476a-93d0-9ad263ccbd9e"),
          "addressUuid" to addressUuid,
          "fullName" to "Jane Doe",
          "gender" to "female",
          "dateOfBirth" to null,
          "age_value" to 29,
          "age_updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "status" to "active",
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "deletedAt" to null,
          "recordedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "syncStatus" to SyncStatus.DONE.name,
          "reminderConsent" to "granted",
          "deletedReason" to null
      ))
    }
  }
}
