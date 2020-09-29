package org.simple.clinic.storage.migrations

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.assertValues
import java.util.UUID

class Migration78AndroidTest: BaseDatabaseMigrationTest(77, 78) {

  @Test
  fun migrating_should_retain_the_saved_user() {
    val phcObvious = UUID.fromString("826aa7e1-372b-4261-8ee3-ea5efc837054")
    val dhNilenso = UUID.fromString("124e2b4e-9aa0-4ae6-8db2-4c6f2ee15470")

    before.execSQL("""
      INSERT INTO "Facility" (
        "uuid", 
        "name", 
        "facilityType", 
        "streetAddress", 
        "villageOrColony", 
        "district", 
        "state", 
        "country",
        "pinCode", 
        "protocolUuid", 
        "groupUuid",
        "createdAt",
        "updatedAt",
        "syncStatus",
        "deletedAt",
        "location_latitude",
        "location_longitude",
        "config_diabetesManagementEnabled",
        "config_teleconsultationEnabled"
      ) VALUES (
        '$phcObvious',
        'PHC Obvious',
        'PHC',
        'Richmond Road',
        'Ashok Nagar',
        'Bangalore Central',
        'Karnataka',
        'India',
        '560025',
        '51e88c38-9029-4650-86fb-eb711f2486ca',
        '7d182169-9c34-4c4e-8cf1-0c7b32478d48',
        '2018-01-01T00:00:00Z',
        '2018-01-01T00:00:00Z',
        'DONE',
        NULL,
        12.9653,
        77.5955413,
        1,
        1
      )
    """)

    before.execSQL("""
      INSERT INTO "Facility" (
        "uuid", 
        "name", 
        "facilityType", 
        "streetAddress", 
        "villageOrColony", 
        "district", 
        "state", 
        "country",
        "pinCode", 
        "protocolUuid", 
        "groupUuid",
        "createdAt",
        "updatedAt",
        "syncStatus",
        "deletedAt",
        "location_latitude",
        "location_longitude",
        "config_diabetesManagementEnabled",
        "config_teleconsultationEnabled"
      ) VALUES (
        '$dhNilenso',
        'DH Nilenso',
        'DH',
        '10th Cross',
        'Indiranagar',
        'HAL',
        'Karnataka',
        'India',
        '560038',
        '51e88c38-9029-4650-86fb-eb711f2486ca',
        '7d182169-9c34-4c4e-8cf1-0c7b32478d48',
        '2018-01-01T00:00:00Z',
        '2018-01-01T00:00:00Z',
        'DONE',
        NULL,
        12.9816341,
        77.6363602,
        1,
        1
      )
    """)

    val userUuid = UUID.fromString("96f9bbac-170f-4cf0-ad67-1d6c62423e5c")
    before.execSQL("""
      INSERT INTO "LoggedInUser" (
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
      ) VALUES (
        '$userUuid',
        'Anish Acharya',
        '1111111111',
        'pin-digest',
        'allowed',
        '2018-01-02T00:00:00Z',
        '2018-01-03T00:00:00Z',
        'LOGGED_IN',
        '$phcObvious',
        '$dhNilenso',
        '1234567890',
        'yes'
      )
    """)

    after
        .query(""" SELECT * FROM "LoggedInUser" """)
        .use { cursor ->
          assertThat(cursor.count).isEqualTo(1)

          cursor.moveToFirst()

          cursor.assertValues(mapOf(
              "uuid" to userUuid,
              "fullName" to "Anish Acharya",
              "phoneNumber" to "1111111111",
              "pinDigest" to "pin-digest",
              "status" to "allowed",
              "createdAt" to "2018-01-02T00:00:00Z",
              "updatedAt" to "2018-01-03T00:00:00Z",
              "loggedInStatus" to "LOGGED_IN",
              "registrationFacilityUuid" to phcObvious,
              "currentFacilityUuid" to dhNilenso,
              "teleconsultPhoneNumber" to "1234567890",
              "capability_canTeleconsult" to "yes"
          ))
        }
  }

  @Test
  fun migrating_should_not_fail_if_there_is_no_user() {
    val phcObvious = UUID.fromString("826aa7e1-372b-4261-8ee3-ea5efc837054")
    val dhNilenso = UUID.fromString("124e2b4e-9aa0-4ae6-8db2-4c6f2ee15470")

    before.execSQL("""
      INSERT INTO "Facility" (
        "uuid", 
        "name", 
        "facilityType", 
        "streetAddress", 
        "villageOrColony", 
        "district", 
        "state", 
        "country",
        "pinCode", 
        "protocolUuid", 
        "groupUuid",
        "createdAt",
        "updatedAt",
        "syncStatus",
        "deletedAt",
        "location_latitude",
        "location_longitude",
        "config_diabetesManagementEnabled",
        "config_teleconsultationEnabled"
      ) VALUES (
        '$phcObvious',
        'PHC Obvious',
        'PHC',
        'Richmond Road',
        'Ashok Nagar',
        'Bangalore Central',
        'Karnataka',
        'India',
        '560025',
        '51e88c38-9029-4650-86fb-eb711f2486ca',
        '7d182169-9c34-4c4e-8cf1-0c7b32478d48',
        '2018-01-01T00:00:00Z',
        '2018-01-01T00:00:00Z',
        'DONE',
        NULL,
        12.9653,
        77.5955413,
        1,
        1
      )
    """)

    before.execSQL("""
      INSERT INTO "Facility" (
        "uuid", 
        "name", 
        "facilityType", 
        "streetAddress", 
        "villageOrColony", 
        "district", 
        "state", 
        "country",
        "pinCode", 
        "protocolUuid", 
        "groupUuid",
        "createdAt",
        "updatedAt",
        "syncStatus",
        "deletedAt",
        "location_latitude",
        "location_longitude",
        "config_diabetesManagementEnabled",
        "config_teleconsultationEnabled"
      ) VALUES (
        '$dhNilenso',
        'DH Nilenso',
        'DH',
        '10th Cross',
        'Indiranagar',
        'HAL',
        'Karnataka',
        'India',
        '560038',
        '51e88c38-9029-4650-86fb-eb711f2486ca',
        '7d182169-9c34-4c4e-8cf1-0c7b32478d48',
        '2018-01-01T00:00:00Z',
        '2018-01-01T00:00:00Z',
        'DONE',
        NULL,
        12.9816341,
        77.6363602,
        1,
        1
      )
    """)

    after
        .query(""" SELECT * FROM "LoggedInUser" """)
        .use { cursor -> assertThat(cursor.count).isEqualTo(0) }
  }
}
