package org.simple.clinic.storage.migrations

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration84AndroidTest: BaseDatabaseMigrationTest(
    fromVersion = 83,
    toVersion = 84
) {

  @Test
  fun it_should_delete_the_local_user_if_they_are_not_logged_in_completely() {
    before.insert("LoggedInUser", mapOf(
        "uuid" to UUID.fromString("3b9b0d6b-6201-4b68-b7b2-920e5e0564e3"),
        "fullName" to "Anish Acharya",
        "phoneNumber" to "1111111111",
        "pinDigest" to "digest",
        "status" to "requested",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
        "loggedInStatus" to "NOT_LOGGED_IN",
        "registrationFacilityUuid" to UUID.fromString("66959b2e-fde6-481a-9fec-93596c8d77cd"),
        "currentFacilityUuid" to UUID.fromString("7321bcd5-783b-4f98-9b41-e99fd8251adf"),
        "teleconsultPhoneNumber" to null,
        "capability_canTeleconsult" to null
    ))

    before
        .query(""" SELECT * FROM "LoggedInUser" """)
        .use { cursor -> assertThat(cursor.count).isEqualTo(1) }

    after
        .query(""" SELECT * FROM "LoggedInUser" """)
        .use { cursor -> assertThat(cursor.count).isEqualTo(0) }
  }

  @Test
  fun it_should_not_delete_the_local_user_if_they_are_logged_in_completely() {
    before.insert("LoggedInUser", mapOf(
        "uuid" to UUID.fromString("3b9b0d6b-6201-4b68-b7b2-920e5e0564e3"),
        "fullName" to "Anish Acharya",
        "phoneNumber" to "1111111111",
        "pinDigest" to "digest",
        "status" to "allowed",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
        "loggedInStatus" to "LOGGED_IN",
        "registrationFacilityUuid" to UUID.fromString("66959b2e-fde6-481a-9fec-93596c8d77cd"),
        "currentFacilityUuid" to UUID.fromString("7321bcd5-783b-4f98-9b41-e99fd8251adf"),
        "teleconsultPhoneNumber" to null,
        "capability_canTeleconsult" to "yes"
    ))

    before
        .query(""" SELECT * FROM "LoggedInUser" """)
        .use { cursor -> assertThat(cursor.count).isEqualTo(1) }

    after
        .query(""" SELECT * FROM "LoggedInUser" """)
        .use { cursor ->
          cursor.moveToFirst()

          cursor.assertValues(mapOf(
              "uuid" to UUID.fromString("3b9b0d6b-6201-4b68-b7b2-920e5e0564e3"),
              "fullName" to "Anish Acharya",
              "phoneNumber" to "1111111111",
              "pinDigest" to "digest",
              "status" to "allowed",
              "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
              "loggedInStatus" to "LOGGED_IN",
              "registrationFacilityUuid" to UUID.fromString("66959b2e-fde6-481a-9fec-93596c8d77cd"),
              "currentFacilityUuid" to UUID.fromString("7321bcd5-783b-4f98-9b41-e99fd8251adf"),
              "teleconsultPhoneNumber" to null,
              "capability_canTeleconsult" to "yes"
          ))
        }
  }

  @Test
  fun it_should_not_delete_the_local_user_if_they_are_waiting_for_login_otp() {
    before.insert("LoggedInUser", mapOf(
        "uuid" to UUID.fromString("3b9b0d6b-6201-4b68-b7b2-920e5e0564e3"),
        "fullName" to "Anish Acharya",
        "phoneNumber" to "1111111111",
        "pinDigest" to "digest",
        "status" to "allowed",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
        "loggedInStatus" to "OTP_REQUESTED",
        "registrationFacilityUuid" to UUID.fromString("66959b2e-fde6-481a-9fec-93596c8d77cd"),
        "currentFacilityUuid" to UUID.fromString("7321bcd5-783b-4f98-9b41-e99fd8251adf"),
        "teleconsultPhoneNumber" to null,
        "capability_canTeleconsult" to "yes"
    ))

    before
        .query(""" SELECT * FROM "LoggedInUser" """)
        .use { cursor -> assertThat(cursor.count).isEqualTo(1) }

    after
        .query(""" SELECT * FROM "LoggedInUser" """)
        .use { cursor ->
          cursor.moveToFirst()

          cursor.assertValues(mapOf(
              "uuid" to UUID.fromString("3b9b0d6b-6201-4b68-b7b2-920e5e0564e3"),
              "fullName" to "Anish Acharya",
              "phoneNumber" to "1111111111",
              "pinDigest" to "digest",
              "status" to "allowed",
              "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
              "loggedInStatus" to "OTP_REQUESTED",
              "registrationFacilityUuid" to UUID.fromString("66959b2e-fde6-481a-9fec-93596c8d77cd"),
              "currentFacilityUuid" to UUID.fromString("7321bcd5-783b-4f98-9b41-e99fd8251adf"),
              "teleconsultPhoneNumber" to null,
              "capability_canTeleconsult" to "yes"
          ))
        }
  }

}
