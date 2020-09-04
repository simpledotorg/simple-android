package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.randomOfEnum
import java.time.Instant
import java.util.UUID

class Migration75AndroidTest : BaseDatabaseMigrationTest(74, 75) {

  @Test
  fun capabilities_should_be_added_to_the_user_table() {
    before.assertColumns("LoggedInUser", setOf(
        "uuid",
        "fullName",
        "phoneNumber",
        "pinDigest",
        "status",
        "createdAt",
        "updatedAt",
        "loggedInStatus",
        "registrationFacilityUuid",
        "currentFacilityUuid",
        "teleconsultPhoneNumber"
    ))

    after.assertColumns("LoggedInUser", setOf(
        "uuid",
        "fullName",
        "phoneNumber",
        "pinDigest",
        "status",
        "createdAt",
        "updatedAt",
        "loggedInStatus",
        "registrationFacilityUuid",
        "currentFacilityUuid",
        "teleconsultPhoneNumber",
        "capability_canTeleconsult"
    ))
  }

  @Test
  fun logged_in_user_data_is_retained_after_the_migration() {
    val facilityUuid = UUID.fromString("fabf0ddf-9f32-40b7-bd3e-3d975783f337")
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
        "uuid" to UUID.fromString("bbcf13e8-1a13-40b0-8556-ce6478b2499f"),
        "fullName" to "Joshua Dawson",
        "phoneNumber" to "999555888",
        "pinDigest" to "1111",
        "status" to "WaitingForApproval",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "loggedInStatus" to "LOGGED_IN",
        "registrationFacilityUuid" to facilityUuid,
        "currentFacilityUuid" to facilityUuid,
        "teleconsultPhoneNumber" to "85459839458"
    ))

    after.query(""" SELECT * FROM LoggedInUser """).use { cursor ->
      cursor.moveToNext()
      cursor.assertValues(mapOf(
          "uuid" to UUID.fromString("bbcf13e8-1a13-40b0-8556-ce6478b2499f"),
          "fullName" to "Joshua Dawson",
          "phoneNumber" to "999555888",
          "pinDigest" to "1111",
          "status" to "WaitingForApproval",
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "loggedInStatus" to "LOGGED_IN",
          "registrationFacilityUuid" to facilityUuid,
          "currentFacilityUuid" to facilityUuid,
          "teleconsultPhoneNumber" to "85459839458",
          "capability_canTeleconsult" to null
      ))
    }

  }


  @Test
  fun capabilities_should_be_added_to_the_Ongoing_login_entry_table() {
    before.assertColumns("OngoingLoginEntry", setOf(
        "uuid",
        "phoneNumber",
        "pin",
        "fullName",
        "pinDigest",
        "registrationFacilityUuid",
        "status",
        "createdAt",
        "updatedAt",
        "teleconsultPhoneNumber",
    ))

    after.assertColumns("OngoingLoginEntry", setOf(
        "uuid",
        "phoneNumber",
        "pin",
        "fullName",
        "pinDigest",
        "registrationFacilityUuid",
        "status",
        "createdAt",
        "updatedAt",
        "teleconsultPhoneNumber",
        "capability_canTeleconsult"
    ))
  }
}
