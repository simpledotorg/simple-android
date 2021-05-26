package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertValues
import org.simple.clinic.assertViewExists
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration88AndroidTest : BaseDatabaseMigrationTest(87, 88) {

  @Test
  fun search_help_column_should_be_added_to_business_id() {
    val patientUuid = UUID.fromString("43fa5f21-a5d2-4fe4-bf10-cbd85d846e00")
    val patientAddressUuid = UUID.fromString("16594b57-7168-470e-9738-371dbe0d588f")
    val businessId = UUID.fromString("8fb8a519-5354-408a-81f9-df9c266bb97c")
    val identifierValue = "767f6234-5f0f-412c-8610-b2eb075e669b"

    before.insert("PatientAddress", mapOf(
        "uuid" to patientAddressUuid,
        "streetAddress" to "1st Sample street",
        "colonyOrVillage" to "Sample Colony",
        "zone" to null,
        "district" to "Sample District",
        "state" to "Punjab",
        "country" to null,
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "deletedAt" to null
    ))
    before.insert("Patient", mapOf(
        "uuid" to patientUuid,
        "addressUuid" to patientAddressUuid,
        "fullName" to "Patient 1",
        "gender" to "male",
        "dateOfBirth" to null,
        "age_value" to 27,
        "age_updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "status" to "active",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "deletedAt" to null,
        "recordedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "syncStatus" to "DONE",
        "reminderConsent" to "granted",
        "deletedReason" to null,
        "registeredFacilityId" to null,
        "assignedFacilityId" to null
    ))
    before.insert("BusinessId", mapOf(
        "uuid" to businessId,
        "patientUuid" to patientUuid,
        "identifier" to identifierValue,
        "identifierType" to "simple_bp_passport",
        "metaVersion" to "org.simple.bppassport.meta.v1",
        "meta" to "",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "deletedAt" to null
    ))

    after.query(""" SELECT * FROM BusinessId """).use { cursor ->
      cursor.moveToNext()
      cursor.assertValues(mapOf(
          "uuid" to businessId,
          "patientUuid" to UUID.fromString("43fa5f21-a5d2-4fe4-bf10-cbd85d846e00"),
          "identifier" to identifierValue,
          "identifierType" to "simple_bp_passport",
          "metaVersion" to "org.simple.bppassport.meta.v1",
          "meta" to "",
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "deletedAt" to null,
          "searchHelp" to "7676234"
      ))
    }
  }

  @Test
  fun patient_search_result_view_should_be_added() {
    before.assertViewExists("PatientSearchResult")
    after.assertViewExists("PatientSearchResult")
  }
}
