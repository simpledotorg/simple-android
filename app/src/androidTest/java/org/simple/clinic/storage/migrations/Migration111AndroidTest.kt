package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration111AndroidTest : BaseDatabaseMigrationTest(110, 111) {

  @Test
  fun migration_to_111_should_add_monthlySuppliesReportsEnabled_fields_and_set_default_values() {
    before.insert("Facility", mapOf(
        "uuid" to UUID.fromString("e575946c-e32a-4f64-9318-92a2d8f64142"),
        "name" to "CHC Obvious",
        "facilityType" to "Mobile",
        "streetAddress" to "Residency Road",
        "villageOrColony" to "Ashok Nagar",
        "district" to "Bengaluru",
        "state" to "Karnataka",
        "country" to "India",
        "pinCode" to "560025",
        "protocolUuid" to UUID.fromString("18aa5db3-089b-410e-bb5b-c1a52cec9d68"),
        "groupUuid" to UUID.fromString("a4ef07d6-94b0-4828-81a9-197b4c2d36e8"),
        "location_latitude" to 12.9653795,
        "location_longitude" to 77.5955252,
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "syncStatus" to "DONE",
        "deletedAt" to null,
        "syncGroup" to "",
        "config_diabetesManagementEnabled" to true,
        "config_teleconsultationEnabled" to true,
        "config_monthlyScreeningReportsEnabled" to false,
    ))

    after.query(""" SELECT * FROM Facility """).use { cursor ->
      cursor.moveToNext()
      cursor.assertValues(mapOf(
          "uuid" to UUID.fromString("e575946c-e32a-4f64-9318-92a2d8f64142"),
          "name" to "CHC Obvious",
          "facilityType" to "Mobile",
          "streetAddress" to "Residency Road",
          "villageOrColony" to "Ashok Nagar",
          "district" to "Bengaluru",
          "state" to "Karnataka",
          "country" to "India",
          "pinCode" to "560025",
          "protocolUuid" to UUID.fromString("18aa5db3-089b-410e-bb5b-c1a52cec9d68"),
          "groupUuid" to UUID.fromString("a4ef07d6-94b0-4828-81a9-197b4c2d36e8"),
          "location_latitude" to 12.9653795,
          "location_longitude" to 77.5955252,
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "syncStatus" to "DONE",
          "deletedAt" to null,
          "syncGroup" to "",
          "config_diabetesManagementEnabled" to true,
          "config_teleconsultationEnabled" to true,
          "config_monthlyScreeningReportsEnabled" to false,
          "config_monthlySuppliesReportsEnabled" to false,
      ))
    }
  }
}
