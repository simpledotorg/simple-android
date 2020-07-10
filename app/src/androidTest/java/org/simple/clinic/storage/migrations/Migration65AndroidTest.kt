package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import org.simple.clinic.patient.SyncStatus
import java.time.Instant
import java.util.UUID

class Migration65AndroidTest : BaseDatabaseMigrationTest(64, 65) {

  @Test
  fun migration_to_65_should_add_teleconsultation_fields_and_set_default_values() {
    before.insert("Facility", mapOf(
        "uuid" to UUID.fromString("3ca79fb1-cc31-432a-a09c-220501f8ab05"),
        "name" to "CHC Obvious",
        "facilityType" to "Mobile",
        "streetAddress" to "Residency Road",
        "villageOrColony" to "Ashok Nagar",
        "district" to "Bengaluru",
        "state" to "Karnataka",
        "country" to "India",
        "pinCode" to "560025",
        "protocolUuid" to UUID.fromString("b1bd4f2f-76c5-446f-a702-492245976117"),
        "groupUuid" to UUID.fromString("deb1d568-4549-46c5-8316-0572509ce3f0"),
        "location_latitude" to 12.9653795,
        "location_longitude" to 77.5955252,
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "syncStatus" to SyncStatus.DONE.name,
        "deletedAt" to null,
        "config_diabetesManagementEnabled" to true
    ))

    after.query(""" SELECT * FROM Facility """).use { cursor ->
      cursor.moveToNext()
      cursor.assertValues(mapOf(
          "uuid" to UUID.fromString("3ca79fb1-cc31-432a-a09c-220501f8ab05"),
          "name" to "CHC Obvious",
          "facilityType" to "Mobile",
          "streetAddress" to "Residency Road",
          "villageOrColony" to "Ashok Nagar",
          "district" to "Bengaluru",
          "state" to "Karnataka",
          "country" to "India",
          "pinCode" to "560025",
          "protocolUuid" to UUID.fromString("b1bd4f2f-76c5-446f-a702-492245976117"),
          "groupUuid" to UUID.fromString("deb1d568-4549-46c5-8316-0572509ce3f0"),
          "location_latitude" to 12.9653795,
          "location_longitude" to 77.5955252,
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "syncStatus" to SyncStatus.DONE.name,
          "deletedAt" to null,
          "config_diabetesManagementEnabled" to true,
          "config_teleconsultationEnabled" to false
      ))
    }
  }
}
