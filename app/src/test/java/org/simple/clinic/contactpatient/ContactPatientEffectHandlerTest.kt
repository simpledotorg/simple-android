package org.simple.clinic.contactpatient

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.phone.Dialer
import org.simple.clinic.util.Just
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.time.LocalDate
import java.util.UUID

class ContactPatientEffectHandlerTest {

  @get:Rule
  val rxErrorRule = RxErrorsRule()

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

  @Test
  fun `when the close screen effect is received, close the sheet`() {
    // when
    testCase.dispatch(CloseScreen)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).closeSheet()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the mark patient as agree to visit effect is received, mark the patient as agreed to visit`() {
    // when
    val appointmentUuid = UUID.fromString("6d47fc9e-76dd-4aa3-b3dd-171e90cadc58")
    testCase.dispatch(MarkPatientAsAgreedToVisit(appointmentUuid))

    // then
    verify(appointmentRepository).markAsAgreedToVisit(appointmentUuid, clock)
    verifyNoMoreInteractions(appointmentRepository)
    testCase.assertOutgoingEvents(PatientMarkedAsAgreedToVisit)
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the show manual date picker effect is received, show the date picker`() {
    // given
    val preselectedDate = LocalDate.parse("2018-01-01")
    val datePickerMin = LocalDate.parse("2017-01-01")
    val datePickerMax = LocalDate.parse("2019-01-01")

    // when
    testCase.dispatch(ShowManualDatePicker(
        preselectedDate = preselectedDate,
        datePickerBounds = datePickerMin..datePickerMax
    ))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showManualDatePicker(preselectedDate, datePickerMin..datePickerMax)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the set reminder effect is received, a reminder date must be set for the appointment for the given date`() {
    // given
    val appointmentUuid = UUID.fromString("10fec427-9509-4237-8493-bef8c3f0a5c2")
    val reminderDate = LocalDate.parse("2018-01-01")

    // when
    testCase.dispatch(SetReminderForAppointment(appointmentUuid, reminderDate))

    // then
    verify(appointmentRepository).createReminder(appointmentUuid, reminderDate)
    verifyNoMoreInteractions(appointmentRepository)
    testCase.assertOutgoingEvents(ReminderSetForAppointment)
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open remove overdue appointment screen effect is received, then open the screen`() {
    // given
    val appointmentId = UUID.fromString("1b91f7e4-aa2e-4ec7-8526-15b57f6025a2")

    // when
    testCase.dispatch(OpenRemoveOverdueAppointmentScreen(appointmentId, patientUuid))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openRemoveOverdueAppointmentScreen(appointmentId, patientUuid)
    verifyNoMoreInteractions(uiActions)
  }
}
