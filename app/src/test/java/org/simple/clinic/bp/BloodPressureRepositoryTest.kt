package org.simple.clinic.bp

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.encounter.EncounterRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.threeten.bp.Instant
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class BloodPressureRepositoryTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val dao = mock<BloodPressureMeasurement.RoomDao>()
  private val testClock = TestUtcClock()
  private val userClock = TestUserClock()
  private val encounterRepository = mock<EncounterRepository>()

  private lateinit var repository: BloodPressureRepository

  @Before
  fun setUp() {
    repository = BloodPressureRepository(
        dao = dao,
        utcClock = testClock,
        userClock = userClock,
        encounterRepository = encounterRepository
    )
  }

  @Test
  fun `when saving a measurement, correctly get IDs for the current user and facility`() {
    val loggedInUser = PatientMocker.loggedInUser()
    val facility = PatientMocker.facility()

    whenever(encounterRepository.saveBloodPressureMeasurement(any())).thenReturn(Completable.complete())

    val patientUuid = UUID.randomUUID()
    repository.saveMeasurement(
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 65,
        loggedInUser = loggedInUser,
        currentFacility = facility,
        recordedAt = Instant.now(testClock)
    ).subscribe()

    verify(encounterRepository).saveBloodPressureMeasurement(check { measurement ->
      assertThat(measurement.systolic).isEqualTo(120)
      assertThat(measurement.diastolic).isEqualTo(65)
      assertThat(measurement.facilityUuid).isEqualTo(facility.uuid)
      assertThat(measurement.patientUuid).isEqualTo(patientUuid)
      assertThat(measurement.createdAt).isEqualTo(Instant.now(testClock))
      assertThat(measurement.updatedAt).isEqualTo(Instant.now(testClock))
      assertThat(measurement.userUuid).isEqualTo(loggedInUser.uuid)
    })
  }
}
