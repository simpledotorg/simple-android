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
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.callresult.CallResult
import org.simple.clinic.overdue.callresult.CallResultRepository
import org.simple.clinic.overdue.callresult.Outcome
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.phone.Dialer
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.RxErrorsRule
import org.simple.sharedTestCode.util.TestUserClock
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

class ContactPatientEffectHandlerTest {

  @get:Rule
  val rxErrorRule = RxErrorsRule()

  private val patientUuid = UUID.fromString("8a490518-a016-4818-b725-22c25dec310b")
  private val patientRepository = mock<PatientRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val callResultRepository = mock<CallResultRepository>()
  private val uuidGenerator = mock<UuidGenerator>()
  private val uiActions = mock<ContactPatientUiActions>()

  private val userClock = TestUserClock(LocalDate.parse("2018-01-01"))
  private val utcClock = TestUtcClock(LocalDate.parse("2018-01-01"))

  private val facility = TestData.facility(
      uuid = UUID.fromString("251deca2-d219-4863-80fc-e7d48cb22b1b"),
      name = "PHC Obvious",
      facilityConfig = FacilityConfig(
          diabetesManagementEnabled = true,
          teleconsultationEnabled = false
      )
  )

  private val user = TestData.loggedInUser(
      uuid = UUID.fromString("ec2452e1-13b3-4d64-b01c-07ade142771e"),
      registrationFacilityUuid = facility.uuid
  )

  private val createReminderForAppointment = CreateReminderForAppointment(
      appointmentRepository = appointmentRepository,
      callResultRepository = callResultRepository,
      utcClock = utcClock,
      currentUser = { user },
      uuidGenerator = uuidGenerator
  )

  private val recordPatientAgreedToVisit = RecordPatientAgreedToVisit(
      appointmentRepository = appointmentRepository,
      callResultRepository = callResultRepository,
      userClock = userClock,
      utcClock = utcClock,
      uuidGenerator = uuidGenerator,
      currentUser = { user }
  )

  private val effectHandler = ContactPatientEffectHandler(
      patientRepository = patientRepository,
      appointmentRepository = appointmentRepository,
      createReminderForAppointment = createReminderForAppointment,
      recordPatientAgreedToVisit = recordPatientAgreedToVisit,
      userClock = userClock,
      schedulers = TestSchedulersProvider.trampoline(),
      currentFacility = { facility },
      callResultRepository = callResultRepository,
      uiActions = uiActions,
      viewEffectsConsumer = ContactPatientViewEffectHandler(uiActions)::handle
  ).build()

  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the load patient profile effect is received, the patient profile must be loaded`() {
    // given
    val contactPatientProfile = TestData.contactPatientProfile(patientUuid = patientUuid)
    whenever(patientRepository.contactPatientProfileImmediate(patientUuid)) doReturn contactPatientProfile

    // when
    testCase.dispatch(LoadContactPatientProfile(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientProfileLoaded(contactPatientProfile))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the load overdue appointment effect is received, the latest overdue appointment for the patient must be loaded`() {
    // given
    val overdueAppointment = Optional.of(TestData.appointment(
        uuid = UUID.fromString("bb291aca-f953-4012-a9c3-aa05685f86f9"),
        patientUuid = patientUuid,
        status = Scheduled
    ))
    val date = LocalDate.now(userClock)
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
    // given
    val callResultId = UUID.fromString("56da1ce0-cad4-4788-ad29-83c550f5f452")
    whenever(uuidGenerator.v4()).thenReturn(callResultId)

    // when
    val appointmentUuid = UUID.fromString("6d47fc9e-76dd-4aa3-b3dd-171e90cadc58")
    val appointment = TestData.appointment(uuid = appointmentUuid)
    testCase.dispatch(MarkPatientAsAgreedToVisit(appointment))

    // then
    verify(appointmentRepository).markAsAgreedToVisit(appointmentUuid, userClock)
    verifyNoMoreInteractions(appointmentRepository)

    val expectedCallResult = CallResult(
        id = callResultId,
        userId = user.uuid,
        appointmentId = appointmentUuid,
        removeReason = null,
        outcome = Outcome.AgreedToVisit,
        timestamps = Timestamps.create(utcClock),
        syncStatus = SyncStatus.PENDING
    )
    verify(callResultRepository).save(listOf(expectedCallResult))
    verifyNoMoreInteractions(callResultRepository)

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
    val appointment = TestData.appointment(uuid = appointmentUuid)
    val reminderDate = LocalDate.parse("2018-01-01")
    val callResultId = UUID.fromString("08556750-df29-4305-8747-fe3e11be58ae")
    whenever(uuidGenerator.v4()).thenReturn(callResultId)

    // when
    testCase.dispatch(SetReminderForAppointment(appointment, reminderDate))

    // then
    verify(appointmentRepository).createReminder(appointmentUuid, reminderDate)
    verifyNoMoreInteractions(appointmentRepository)

    val expectedCallResult = CallResult(
        id = callResultId,
        userId = user.uuid,
        appointmentId = appointmentUuid,
        removeReason = null,
        outcome = Outcome.RemindToCallLater,
        timestamps = Timestamps.create(utcClock),
        syncStatus = SyncStatus.PENDING
    )
    verify(callResultRepository).save(listOf(expectedCallResult))
    verifyNoMoreInteractions(callResultRepository)

    testCase.assertOutgoingEvents(ReminderSetForAppointment)
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open remove overdue appointment screen effect is received, then open the screen`() {
    // given
    val appointmentId = UUID.fromString("1b91f7e4-aa2e-4ec7-8526-15b57f6025a2")
    val appointment = TestData.appointment(uuid = appointmentId, patientUuid = patientUuid)

    // when
    testCase.dispatch(OpenRemoveOverdueAppointmentScreen(appointment))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openRemoveOverdueAppointmentScreen(appointment)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the load current facility effect is received, then the current facility must be loaded`() {
    // when
    testCase.dispatch(LoadCurrentFacility)

    // then
    testCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when load call result for appointment effect is received, then load call result for appointment`() {
    // given
    val appointmentId = UUID.fromString("02642b80-1241-4862-a123-26f9dcb933ff")
    val callResult = TestData.callResult(id = UUID.fromString("70e67c46-94ce-4c8c-ad19-22f383896ea8"), appointmentId = appointmentId)
    whenever(callResultRepository.callResultForAppointment(appointmentId)).thenReturn(Optional.of(callResult))

    // when
    testCase.dispatch(LoadCallResultForAppointment(appointmentId = appointmentId))

    // then
    testCase.assertOutgoingEvents(CallResultForAppointmentLoaded(Optional.of(callResult)))
    verifyZeroInteractions(uiActions)
  }
}
