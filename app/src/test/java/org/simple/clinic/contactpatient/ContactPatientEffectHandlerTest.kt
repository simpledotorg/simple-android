package org.simple.clinic.contactpatient

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.phone.Dialer
import org.simple.clinic.util.Just
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.threeten.bp.LocalDate
import java.util.UUID

class ContactPatientEffectHandlerTest {

  private val patientUuid = UUID.fromString("8a490518-a016-4818-b725-22c25dec310b")
  private val patientRepository = mock<PatientRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val uiActions = mock<ContactPatientUiActions>()

  private val clock = TestUserClock(LocalDate.parse("2018-01-01"))

  private val effectHandler = ContactPatientEffectHandler(
      patientRepository = patientRepository,
      appointmentRepository = appointmentRepository,
      clock = clock,
      schedulers = TrampolineSchedulersProvider(),
      uiActions = uiActions
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the load patient profile effect is received, the patient profile must be loaded`() {
    // given
    val patientProfile = TestData.patientProfile(patientUuid = patientUuid)
    whenever(patientRepository.patientProfileImmediate(patientUuid)) doReturn Just(patientProfile)

    // when
    testCase.dispatch(LoadPatientProfile(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientProfileLoaded(patientProfile))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the load overdue appointment effect is received, the latest overdue appointment for the patient must be loaded`() {
    // given
    val overdueAppointment = Just(TestData.overdueAppointment(
        appointmentUuid = UUID.fromString("bb291aca-f953-4012-a9c3-aa05685f86f9"),
        patientUuid = patientUuid
    ))
    val date = LocalDate.now(clock)
    whenever(appointmentRepository.latestOverdueAppointmentForPatient(patientUuid, date)) doReturn overdueAppointment

    // when
    testCase.dispatch(LoadLatestOverdueAppointment(patientUuid))

    // then
    testCase.assertOutgoingEvents(OverdueAppointmentLoaded(overdueAppointment))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the call patient directly with automatic dialer effect is received, the direct phone call must be made with the automatic dialer`() {
    // when
    val patientPhoneNumber = "1234567890"
    testCase.dispatch(DirectCallWithAutomaticDialer(patientPhoneNumber))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).directlyCallPatient(patientPhoneNumber, Dialer.Automatic)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the call patient directly with manual dialer effect is received, the direct phone call must be made with the manual dialer`() {
    // when
    val patientPhoneNumber = "1234567890"
    testCase.dispatch(DirectCallWithManualDialer(patientPhoneNumber))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).directlyCallPatient(patientPhoneNumber, Dialer.Manual)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the call patient masked with automatic dialer effect is received, the masked phone call must be made with the automatic dialer`() {
    // when
    val patientPhoneNumber = "1234567890"
    val proxyNumber = "0987654321"
    testCase.dispatch(MaskedCallWithAutomaticDialer(patientPhoneNumber = patientPhoneNumber, proxyPhoneNumber = proxyNumber))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).maskedCallPatient(patientPhoneNumber = patientPhoneNumber, proxyNumber = proxyNumber, dialer = Dialer.Automatic)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the call patient masked with manual dialer effect is received, the masked phone call must be made with the manual dialer`() {
    // when
    val patientPhoneNumber = "1234567890"
    val proxyNumber = "0987654321"
    testCase.dispatch(MaskedCallWithManualDialer(patientPhoneNumber = patientPhoneNumber, proxyPhoneNumber = proxyNumber))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).maskedCallPatient(patientPhoneNumber = patientPhoneNumber, proxyNumber = proxyNumber, dialer = Dialer.Manual)
    verifyNoMoreInteractions(uiActions)
  }
}
