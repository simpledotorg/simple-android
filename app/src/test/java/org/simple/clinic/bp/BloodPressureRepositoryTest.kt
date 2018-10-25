package org.simple.clinic.bp

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.TestClock
import org.threeten.bp.Clock
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class BloodPressureRepositoryTest {

  private val dao = mock<BloodPressureMeasurement.RoomDao>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val testClock = TestClock()

  private lateinit var repository: BloodPressureRepository

  @Before
  fun setUp() {
    repository = BloodPressureRepository(dao, userSession, facilityRepository, testClock)
  }

  @Test
  fun `when saving a measurement, correctly get IDs for the current user and facility`() {
    val aUuid = UUID.randomUUID()
    val loggedInUser = PatientMocker.loggedInUser(aUuid)

    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(loggedInUser))

    val facility = PatientMocker.facility()
    whenever(facilityRepository.currentFacility(userSession)).thenReturn(Observable.just(facility))

    val patientUuid = UUID.randomUUID()
    repository.saveMeasurement(patientUuid, systolic = 120, diastolic = 65).subscribe()

    verify(dao).save(check {
      assertThat(it.first().systolic).isEqualTo(120)
      assertThat(it.first().diastolic).isEqualTo(65)
      assertThat(it.first().facilityUuid).isEqualTo(facility.uuid)
      assertThat(it.first().patientUuid).isEqualTo(patientUuid)

      assertThat(it.first().userUuid).isEqualTo(aUuid)
    })
  }

  @Test
  @Parameters(value = [
    "PENDING, false",
    "IN_FLIGHT, false",
    "INVALID, true",
    "DONE, true"])
  fun `when merging measurements with server records, ignore records that already exist locally and are syncing or pending-sync`(
      syncStatusOfLocalCopy: SyncStatus,
      serverRecordExpectedToBeSaved: Boolean
  ) {
    val bpUuid = UUID.randomUUID()

    val localCopy = PatientMocker.bp(bpUuid, syncStatus = syncStatusOfLocalCopy)
    whenever(dao.getOne(bpUuid)).thenReturn(localCopy)

    val serverBp = PatientMocker.bp(bpUuid, syncStatus = SyncStatus.DONE).toPayload()
    repository.mergeWithLocalData(listOf(serverBp)).blockingAwait()

    if (serverRecordExpectedToBeSaved) {
      verify(dao).save(argThat { isNotEmpty() })
    } else {
      verify(dao).save(argThat { isEmpty() })
    }
  }
}
