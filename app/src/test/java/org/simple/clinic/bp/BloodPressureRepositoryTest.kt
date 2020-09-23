package org.simple.clinic.bp

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import java.time.Instant
import java.util.UUID

class BloodPressureRepositoryTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val dao = mock<BloodPressureMeasurement.RoomDao>()
  private val testClock = TestUtcClock()

  private lateinit var repository: BloodPressureRepository

  @Before
  fun setUp() {
    repository = BloodPressureRepository(dao, testClock)
  }

  @Test
  fun `when saving a measurement, correctly get IDs for the current user and facility`() {
    val loggedInUser = TestData.loggedInUser()
    val facility = TestData.facility()

    val patientUuid = UUID.fromString("53e2f919-eea8-44b1-a325-b1ab094766f5")
    val bpUuid = UUID.fromString("6d1b8875-c659-4dbd-a5c9-d642e0960504")
    val reading = BloodPressureReading(120, 65)
    repository.saveMeasurementBlocking(
        patientUuid = patientUuid,
        reading = reading,
        loggedInUser = loggedInUser,
        currentFacility = facility,
        recordedAt = Instant.now(testClock),
        uuid = bpUuid)

    verify(dao).save(listOf(
        BloodPressureMeasurement(
            uuid = bpUuid,
            syncStatus = SyncStatus.PENDING,
            patientUuid = patientUuid,
            reading = reading,
            userUuid = loggedInUser.uuid,
            facilityUuid = facility.uuid,
            createdAt = Instant.now(testClock),
            updatedAt = Instant.now(testClock),
            recordedAt = Instant.now(testClock),
            deletedAt = null
        )
    ))
  }
}
