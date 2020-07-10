package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration62AndroidTest : BaseDatabaseMigrationTest(61, 62) {

  @Test
  fun it_should_set_the_registration_and_current_facility_of_the_user_to_the_current_facility() {
    val userUuid = UUID.fromString("ceac168b-9d64-4f6b-9c3a-e1cf6b81b867")
    val currentFacilityUuid = UUID.fromString("6e5ce98c-3e55-43fa-a619-4de19bcf2f29")
    val otherFacilityUuid = UUID.fromString("aab13afc-7994-4e0b-b645-0a94eb2971fe")

    before.insert(
        tableName = "LoggedInUser",
        valuesMap = mapOf(
            "uuid" to userUuid,
            "fullName" to "Anish Acharya",
            "phoneNumber" to "1234567890",
            "pinDigest" to "digest",
            "status" to "allowed",
            "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
            "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
            "loggedInStatus" to "LOGGED_IN"
        )
    )

    before.insert(
        tableName = "Facility",
        valuesMap = mapOf(
            "uuid" to currentFacilityUuid,
            "name" to "Current Facility",
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
            "syncStatus" to "DONE"
        )
    )

    before.insert(
        tableName = "Facility",
        valuesMap = mapOf(
            "uuid" to otherFacilityUuid,
            "name" to "Other Facility",
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
            "syncStatus" to "DONE"
        )
    )

    before.insert(
        tableName = "LoggedInUserFacilityMapping",
        valuesMap = mapOf(
            "userUuid" to userUuid,
            "facilityUuid" to otherFacilityUuid,
            "isCurrentFacility" to false
        )
    )

    before.insert(
        tableName = "LoggedInUserFacilityMapping",
        valuesMap = mapOf(
            "userUuid" to userUuid,
            "facilityUuid" to currentFacilityUuid,
            "isCurrentFacility" to true
        )
    )

    after
        .query(""" SELECT * FROM "LoggedInUser" WHERE "uuid" = '$userUuid' """)
        .use { cursor ->
          cursor.moveToFirst()
          cursor.assertValues(
              mapOf(
                  "uuid" to userUuid,
                  "fullName" to "Anish Acharya",
                  "phoneNumber" to "1234567890",
                  "pinDigest" to "digest",
                  "status" to "allowed",
                  "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
                  "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
                  "loggedInStatus" to "LOGGED_IN",
                  "registrationFacilityUuid" to currentFacilityUuid,
                  "currentFacilityUuid" to currentFacilityUuid
              )
          )
        }
  }

  @Test
  fun it_should_delete_the_user_facility_mapping_table() {
    before.assertTableExists("LoggedInUserFacilityMapping")

    after.assertTableDoesNotExist("LoggedInUserFacilityMapping")
  }
}
