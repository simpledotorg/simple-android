package org.simple.clinic.storage.migrations

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration81AndroidTest : BaseDatabaseMigrationTest(
    fromVersion = 80,
    toVersion = 81
) {

  @Test
  fun it_should_set_the_current_sync_group_to_an_empty_string() {
    before.insert("Facility", mapOf(
        "uuid" to UUID.fromString("0826d20a-1145-457d-806b-5a3e31743c25"),
        "name" to "PHC Obvious",
        "facilityType" to "PHC",
        "streetAddress" to "Richmond Road",
        "villageOrColony" to "Ashok Nagar",
        "district" to "Bangalore Central",
        "state" to "Karnataka",
        "country" to "India",
        "pinCode" to "560025",
        "protocolUuid" to UUID.fromString("2d5983bc-62f6-4b6c-8d4d-2ab5b54c7d9b"),
        "groupUuid" to UUID.fromString("24f5ecca-da1f-4c1d-951f-055db4cb1c2b"),
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
        "syncStatus" to "DONE",
        "deletedAt" to Instant.parse("2018-01-01T00:00:02Z"),
        "location_latitude" to 12.9653,
        "location_longitude" to 77.5955413,
        "config_diabetesManagementEnabled" to true,
        "config_teleconsultationEnabled" to false,
        "syncGroup" to null
    ))

    after
        .query(""" SELECT * FROM "Facility" """)
        .use { cursor ->
          assertThat(cursor.count).isEqualTo(1)

          cursor.moveToFirst()
          cursor.assertValues(mapOf(
              "uuid" to UUID.fromString("0826d20a-1145-457d-806b-5a3e31743c25"),
              "name" to "PHC Obvious",
              "facilityType" to "PHC",
              "streetAddress" to "Richmond Road",
              "villageOrColony" to "Ashok Nagar",
              "district" to "Bangalore Central",
              "state" to "Karnataka",
              "country" to "India",
              "pinCode" to "560025",
              "protocolUuid" to UUID.fromString("2d5983bc-62f6-4b6c-8d4d-2ab5b54c7d9b"),
              "groupUuid" to UUID.fromString("24f5ecca-da1f-4c1d-951f-055db4cb1c2b"),
              "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
              "syncStatus" to "DONE",
              "deletedAt" to Instant.parse("2018-01-01T00:00:02Z"),
              "location_latitude" to 12.9653,
              "location_longitude" to 77.5955413,
              "config_diabetesManagementEnabled" to true,
              "config_teleconsultationEnabled" to false,
              "syncGroup" to ""
          ))
        }
  }
}
