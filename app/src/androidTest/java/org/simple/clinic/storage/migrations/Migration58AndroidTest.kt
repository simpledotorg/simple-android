package org.simple.clinic.storage.migrations

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import org.simple.clinic.patient.SyncStatus
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class Migration58AndroidTest : BaseDatabaseMigrationTest(57, 58) {

  @Inject
  lateinit var testData: TestData

  @Before
  override fun setUp() {
    super.setUp()
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun migrating_to_58_should_add_the_facility_config_model_to_all_facilities_with_the_diabetes_management_flag_set_to_false() {
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
        "deletedAt" to null
    ))
    before.insert("Facility", mapOf(
        "uuid" to UUID.fromString("9042ff8c-63bc-4e96-86d7-f4851c7940f7"),
        "name" to "CHC Nilenso",
        "facilityType" to "Server",
        "streetAddress" to "10th Cross",
        "villageOrColony" to "Indiranagar",
        "district" to "Bengaluru",
        "state" to "Karnataka",
        "country" to "India",
        "pinCode" to "560038",
        "protocolUuid" to UUID.fromString("c087fb6e-0898-425c-a174-1d3d2c162b0a"),
        "groupUuid" to UUID.fromString("77b8a80c-ce93-4def-93d5-b31ad9d78d95"),
        "location_latitude" to 12.9816393,
        "location_longitude" to 77.6363549,
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "syncStatus" to SyncStatus.DONE.name,
        "deletedAt" to null
    ))

    after.query(""" SELECT * FROM Facility """).use { cursor ->
      assertThat(cursor.count).isEqualTo(2)

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
          "config_diabetesManagementEnabled" to false
      ))

      cursor.moveToNext()
      cursor.assertValues(mapOf(
          "uuid" to UUID.fromString("9042ff8c-63bc-4e96-86d7-f4851c7940f7"),
          "name" to "CHC Nilenso",
          "facilityType" to "Server",
          "streetAddress" to "10th Cross",
          "villageOrColony" to "Indiranagar",
          "district" to "Bengaluru",
          "state" to "Karnataka",
          "country" to "India",
          "pinCode" to "560038",
          "protocolUuid" to UUID.fromString("c087fb6e-0898-425c-a174-1d3d2c162b0a"),
          "groupUuid" to UUID.fromString("77b8a80c-ce93-4def-93d5-b31ad9d78d95"),
          "location_latitude" to 12.9816393,
          "location_longitude" to 77.6363549,
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "syncStatus" to SyncStatus.DONE.name,
          "deletedAt" to null,
          "config_diabetesManagementEnabled" to false
      ))
    }
  }
}
