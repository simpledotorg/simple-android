package org.simple.clinic.removeoverdueappointment

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.callresult.CallResult
import org.simple.clinic.overdue.callresult.CallResultRepository
import org.simple.clinic.overdue.callresult.Outcome
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.Instant
import java.util.UUID

class RemoveOverdueEffectHandlerTest {

  private val appointmentRepository = mock<AppointmentRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val uiActions = mock<RemoveOverdueUiActions>()
  private val uuidGenerator = mock<UuidGenerator>()
  private val callResultRepository = mock<CallResultRepository>()
  private val utcClock = TestUtcClock(Instant.parse("2018-01-01T00:00:00Z"))
  private val user = TestData.loggedInUser(
      uuid = UUID.fromString("95455934-8399-479f-b1a7-0ebec9d04c9e"),
      currentFacilityUuid = UUID.fromString("b7e68669-5352-4d14-9f74-1037709c92d4")
  )
  private val cancelAppointmentWithReason = CancelAppointmentWithReason(
      appointmentRepository = appointmentRepository,
      callResultRepository = callResultRepository,
      uuidGenerator = uuidGenerator,
      utcClock = utcClock,
      currentUser = { user }
  )

  private val effectHandler = RemoveOverdueEffectHandler(
      appointmentRepository = appointmentRepository,
      patientRepository = patientRepository,
      cancelAppointmentWithReason = cancelAppointmentWithReason,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      viewEffectsConsumer = RemoveOverdueViewEffectHandler(uiActions)::handle
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when mark patient as visited effect is received, then mark patient as visited`() {
    // given
    val appointmentId = UUID.fromString("e1c5ab51-da5d-4f58-bd42-d15c52e40c77")

    // when
    testCase.dispatch(MarkPatientAsVisited(appointmentId))

    // then
    verify(appointmentRepository).markAsAlreadyVisited(appointmentId)
    verifyNoMoreInteractions(appointmentRepository)

    verifyNoInteractions(uiActions)

    testCase.assertOutgoingEvents(PatientMarkedAsVisited)
  }

  @Test
  fun `when mark patient as dead effect is received, then update patient status`() {
    // given
    val patientId = UUID.fromString("6a87ea63-2ef4-4d27-b8ef-a07f1706cb67")
    val appointmentId = UUID.fromString("0affc37a-7344-493e-8768-6175c96c905e")

    // when
    testCase.dispatch(MarkPatientAsDead(patientId, appointmentId))

    // then
    verifyNoInteractions(appointmentRepository)

    verify(patientRepository).updatePatientStatusToDead(patientId)
    verifyNoMoreInteractions(patientRepository)

    verifyNoInteractions(uiActions)

    testCase.assertOutgoingEvents(PatientMarkedAsDead)
  }

  @Test
  fun `when cancel appointment effect is received, then cancel the appointment`() {
    // given
    val appointmentId = UUID.fromString("3a908737-17c8-44e6-b4f9-03a46a185189")
    val patientId = UUID.fromString("c1514de4-e11c-4637-9d68-298d5bb61f64")
    val facilityId = UUID.fromString("013ae0b4-7204-42aa-8ee7-3abbc8dde7a2")
    val appointment = TestData.appointment(uuid = appointmentId, patientUuid = patientId, facilityUuid = facilityId)
    val cancelReason = AppointmentCancelReason.random()

    val callResultId = UUID.fromString("3d3fb748-0ad5-4f8b-b3ad-e12856094a2b")
    whenever(uuidGenerator.v4()).thenReturn(callResultId)

    // when
    testCase.dispatch(CancelAppointment(appointment, cancelReason))

    // then
    verify(appointmentRepository).cancelWithReason(appointmentId, cancelReason)
    verifyNoMoreInteractions(appointmentRepository)

    val expectedCallResult = CallResult(
        id = callResultId,
        userId = user.uuid,
        patientId = patientId,
        facilityId = user.currentFacilityUuid,
        appointmentId = appointmentId,
        removeReason = cancelReason,
        outcome = Outcome.RemovedFromOverdueList,
        timestamps = Timestamps.create(utcClock),
        syncStatus = SyncStatus.PENDING
    )
    verify(callResultRepository).save(listOf(expectedCallResult))
    verifyNoMoreInteractions(callResultRepository)

    verifyNoInteractions(uiActions)

    testCase.assertOutgoingEvents(AppointmentMarkedAsCancelled)
  }

  @Test
  fun `when mark patient as moved to private effect is received, then mark patient as moved to private`() {
    // given
    val patientId = UUID.fromString("4968aff1-75d1-4711-b111-611dff231f23")

    // when
    testCase.dispatch(MarkPatientAsMovedToPrivate(patientId))

    // then
    verify(patientRepository).updatePatientStatusToMigrated(patientId)
    verifyNoMoreInteractions(patientRepository)

    verifyNoInteractions(uiActions)

    testCase.assertOutgoingEvents(PatientMarkedAsMigrated(AppointmentCancelReason.MovedToPrivatePractitioner))
  }

  @Test
  fun `when mark patient as transferred to another facility, then update the patient status`() {
    // given
    val patientId = UUID.fromString("4a807b0b-4457-439b-b370-203377027057")

    // when
    testCase.dispatch(MarkPatientAsTransferredToAnotherFacility(patientId))

    // then
    verify(patientRepository).updatePatientStatusToMigrated(patientId)
    verifyNoMoreInteractions(patientRepository)

    verifyNoInteractions(uiActions)

    testCase.assertOutgoingEvents(PatientMarkedAsMigrated(AppointmentCancelReason.TransferredToAnotherPublicHospital))
  }

  @Test
  fun `when go back after appointment removed effect is received, then go back with a result`() {
    // when
    testCase.dispatch(GoBackAfterAppointmentRemoval)

    // then
    verify(uiActions).goBackAfterAppointmentRemoval()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when mark patient as refused to come back effect is received, then update the patient status`() {
    // given
    val patientId = UUID.fromString("cee7568e-7a82-46cc-afdc-310fc8552615")

    // when
    testCase.dispatch(MarkPatientAsRefusedToComeBack(patientId))

    // then
    verify(patientRepository).updatePatientStatusToMigrated(patientId)
    verifyNoMoreInteractions(patientRepository)

    verifyNoInteractions(uiActions)

    testCase.assertOutgoingEvents(PatientMarkedAsMigrated(AppointmentCancelReason.RefusedToComeBack))
  }
}
