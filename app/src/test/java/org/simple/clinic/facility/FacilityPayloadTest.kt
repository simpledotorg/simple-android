package org.simple.clinic.facility

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.location.Coordinates
import org.simple.clinic.patient.SyncStatus

class FacilityPayloadTest {

  @Test
  fun `when converting to database model and latitude is missing then location coordinates should be empty`() {
    val payload = TestData.facilityPayload(locationLatitude = null, locationLongitude = 73.537524)
    val databaseModel = payload.toDatabaseModel(SyncStatus.DONE)
    assertThat(databaseModel.location).isNull()
  }

  @Test
  fun `when converting to database model and longitude is missing then location coordinates should be empty`() {
    val payload = TestData.facilityPayload(locationLatitude = 1.908537, locationLongitude = null)
    val databaseModel = payload.toDatabaseModel(SyncStatus.DONE)
    assertThat(databaseModel.location).isNull()
  }

  @Test
  fun `when converting to database model and coordinates are present then location coordinates should not be empty`() {
    val payload = TestData.facilityPayload(locationLatitude = 1.908537, locationLongitude = 73.537524)
    val databaseModel = payload.toDatabaseModel(SyncStatus.DONE)
    assertThat(databaseModel.location).isEqualTo(Coordinates(latitude = 1.908537, longitude = 73.537524))
  }
}
