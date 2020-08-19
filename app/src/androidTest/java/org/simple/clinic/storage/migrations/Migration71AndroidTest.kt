package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration71AndroidTest : BaseDatabaseMigrationTest(70, 71) {

  @Test
  fun user_information_should_be_retained_after_the_migration() {
    val facilityUuid = UUID.fromString("eac9f788-1a8c-4f29-87ff-5d16d5a4eeba")
    before.insert("Facility", mapOf(
        "uuid" to facilityUuid,
        "name" to "PHC Obvious",
        "facilityType" to null,
        "streetAddress" to null,
        "villageOrColony" to null,
        "district" to "Bathinda",
        "state" to "Punjab",
        "country" to "India",
        "pinCode" to null,
        "protocolUuid" to null,
        "groupUuid" to null,
        "location_latitude" to null,
        "location_longitude" to null,
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
        "deletedAt" to null,
        "config_diabetesManagementEnabled" to true,
        "config_teleconsultationEnabled" to true,
        "syncStatus" to "DONE"
    ))
    before.insert("LoggedInUser", mapOf(
        "uuid" to UUID.fromString("dfe2653f-62d7-4b05-b2c0-ae596361de51"),
        "fullName" to "John Doe",
        "phoneNumber" to "1111111111",
        "pinDigest" to "1111",
        "status" to "allowed",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "loggedInStatus" to "LOGGED_IN",
        "registrationFacilityUuid" to facilityUuid,
        "currentFacilityUuid" to facilityUuid
    ))

    after.query(""" SELECT * FROM LoggedInUser """).use { cursor ->
      cursor.moveToNext()
      cursor.assertValues(mapOf(
          "uuid" to UUID.fromString("dfe2653f-62d7-4b05-b2c0-ae596361de51"),
          "fullName" to "John Doe",
          "phoneNumber" to "1111111111",
          "pinDigest" to "1111",
          "status" to "allowed",
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "loggedInStatus" to "LOGGED_IN",
          "registrationFacilityUuid" to facilityUuid,
          "currentFacilityUuid" to facilityUuid,
          "teleconsultPhoneNumber" to null
      ))
    }
  }

  @Test
  fun ongoing_login_entry_information_should_be_retained_after_the_migration() {
    before.insert("OngoingLoginEntry", mapOf(
        "uuid" to UUID.fromString("fc5090a4-c4e8-4ada-a163-0921741acee6"),
        "phoneNumber" to "1111111111",
        "pin" to "1111",
        "fullName" to "John Doe",
        "pinDigest" to "1111",
        "status" to "allowed",
        "registrationFacilityUuid" to UUID.fromString("d2e319bd-fe2d-4439-9db1-d2aaa0d4c59e"),
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z")
    ))

    after.query(""" SELECT * FROM OngoingLoginEntry """).use { cursor ->
      cursor.moveToNext()
      cursor.assertValues(mapOf(
          "uuid" to UUID.fromString("fc5090a4-c4e8-4ada-a163-0921741acee6"),
          "phoneNumber" to "1111111111",
          "pin" to "1111",
          "fullName" to "John Doe",
          "pinDigest" to "1111",
          "status" to "allowed",
          "registrationFacilityUuid" to UUID.fromString("d2e319bd-fe2d-4439-9db1-d2aaa0d4c59e"),
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "teleconsultPhoneNumber" to null
      ))
    }
  }
}
