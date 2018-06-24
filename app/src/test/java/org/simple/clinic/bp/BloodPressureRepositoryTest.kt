package org.simple.clinic.bp

import com.f2prateek.rx.preferences2.Preference
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
import org.simple.clinic.user.LoggedInUser
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class BloodPressureRepositoryTest {

  private val dao = mock<BloodPressureMeasurement.RoomDao>()
  private val loggedInUserRxPref = mock<Preference<Optional<LoggedInUser>>>()
  private val facilityRepository = mock<FacilityRepository>()

  private lateinit var repository: BloodPressureRepository

  @Before
  fun setUp() {
    repository = BloodPressureRepository(dao, loggedInUserRxPref, facilityRepository)
  }

  @Test
  fun `when saving a measurement, correctly get IDs for the current user and facility`() {

    // TODO: Uncomment this once user login works for real! Make user login call before running tests!
//    val aUuid = UUID.randomUUID()
//    val loggedInUser = Just(LoggedInUser(aUuid, "a name", "a phone", "a hash", mock(), mock(), mock()))

    whenever(loggedInUserRxPref.asObservable()).thenReturn(Observable.just(Just(dummyUserForBpTests())))

    val facility = PatientMocker.facility()
    whenever(facilityRepository.currentFacility()).thenReturn(Observable.just(facility))

    val patientUuid = UUID.randomUUID()
    repository.saveMeasurement(patientUuid, systolic = 120, diastolic = 65).subscribe()

    verify(dao).save(check {
      assertThat(it.first().systolic).isEqualTo(120)
      assertThat(it.first().diastolic).isEqualTo(65)
      assertThat(it.first().facilityUuid).isEqualTo(facility.uuid)
      assertThat(it.first().patientUuid).isEqualTo(patientUuid)

      // TODO: Uncomment this once user login works for real! Make user login call before running tests!
//      assertThat(it.first().userUuid).isEqualTo(aUuid)
      assertThat(it.first().userUuid).isEqualTo(dummyUserForBpTests().uuid)
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
