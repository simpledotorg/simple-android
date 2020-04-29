package org.simple.clinic.bp

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.threeten.bp.Instant
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
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
    repository.saveMeasurement(
        patientUuid = patientUuid,
        reading = BloodPressureReading(120, 65),
        loggedInUser = loggedInUser,
        currentFacility = facility,
        recordedAt = Instant.now(testClock)
    ).subscribe()

    verify(dao).save(check {
      val measurement = it.first()
      assertThat(measurement.reading.systolic).isEqualTo(120)
      assertThat(measurement.reading.diastolic).isEqualTo(65)
      assertThat(measurement.facilityUuid).isEqualTo(facility.uuid)
      assertThat(measurement.patientUuid).isEqualTo(patientUuid)
      assertThat(measurement.createdAt).isEqualTo(Instant.now(testClock))
      assertThat(measurement.updatedAt).isEqualTo(Instant.now(testClock))
      assertThat(measurement.userUuid).isEqualTo(loggedInUser.uuid)
    })
  }

  @Test
  @Parameters(value = [
    "PENDING, false",
    "INVALID, true",
    "DONE, true"])
  fun `when merging measurements with server records, ignore records that already exist locally and are syncing or pending-sync`(
      syncStatusOfLocalCopy: SyncStatus,
      serverRecordExpectedToBeSaved: Boolean
  ) {
    val bpUuid = UUID.randomUUID()

    val localCopy = TestData.bloodPressureMeasurement(bpUuid, syncStatus = syncStatusOfLocalCopy)
    whenever(dao.getOne(bpUuid)).doReturn(localCopy)

    val serverBp = TestData.bloodPressureMeasurement(bpUuid, syncStatus = SyncStatus.DONE).toPayload()
    repository.mergeWithLocalData(listOf(serverBp)).blockingAwait()

    if (serverRecordExpectedToBeSaved) {
      verify(dao).save(argThat { isNotEmpty() })
    } else {
      verify(dao).save(argThat { isEmpty() })
    }
  }
}
