package org.simple.clinic.removeoverdueappointment

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class RemoveOverdueEffectHandlerTest {

  private val appointmentRepository = mock<AppointmentRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val effectHandler = RemoveOverdueEffectHandler(
      appointmentRepository = appointmentRepository,
      patientRepository = patientRepository,
      schedulersProvider = TestSchedulersProvider.trampoline()
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

    testCase.assertOutgoingEvents(PatientMarkedAsVisited)
  }

  @Test
  fun `when mark patient as dead effect is received, then cancel appointment and update patient status`() {
    // given
    val patientId = UUID.fromString("6a87ea63-2ef4-4d27-b8ef-a07f1706cb67")
    val appointmentId = UUID.fromString("0affc37a-7344-493e-8768-6175c96c905e")

    // when
    testCase.dispatch(MarkPatientAsDead(patientId, appointmentId))

    // then
    verify(appointmentRepository).cancelWithReason(appointmentId, AppointmentCancelReason.Dead)
    verifyNoMoreInteractions(appointmentRepository)

    verify(patientRepository).updatePatientStatusToDead(patientId)
    verifyNoMoreInteractions(patientRepository)

    testCase.assertOutgoingEvents(PatientMarkedAsDead)
  }

  @Test
  fun `when cancel appointment effect is received, then cancel the appointment`() {
    // given
    val appointmentId = UUID.fromString("3a908737-17c8-44e6-b4f9-03a46a185189")
    val cancelReason = AppointmentCancelReason.random()

    // when
    testCase.dispatch(CancelAppointment(appointmentId, cancelReason))

    // then
    verify(appointmentRepository).cancelWithReason(appointmentId, cancelReason)
    verifyNoMoreInteractions(appointmentRepository)

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

    testCase.assertOutgoingEvents(PatientMarkedAsMigrated(AppointmentCancelReason.MovedToPrivatePractitioner))
  }
}
