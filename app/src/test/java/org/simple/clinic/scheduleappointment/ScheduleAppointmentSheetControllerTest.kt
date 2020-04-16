package org.simple.clinic.scheduleappointment

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.Appointment.AppointmentType.Automatic
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.protocol.Protocol
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.scheduleappointment.TimeToAppointment.Days
import org.simple.clinic.scheduleappointment.TimeToAppointment.Months
import org.simple.clinic.scheduleappointment.TimeToAppointment.Weeks
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class ScheduleAppointmentSheetControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val sheet = mock<ScheduleAppointmentSheet>()
  private val repository = mock<AppointmentRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val protocolRepository = mock<ProtocolRepository>()
  private val userSession = mock<UserSession>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val today = LocalDate.parse("2019-01-01")
  private val clock = TestUserClock(today)
  private val user = TestData.loggedInUser()
  private val patientUuid = UUID.fromString("d44bf81f-4369-4bbc-a51b-52d88c54f065")
  private val protocolUuid = UUID.fromString("8782e890-2fb0-4204-8647-6d20006cec02")
  private val facility = TestData.facility(protocolUuid = protocolUuid)
  private val protocol = TestData.protocol(protocolUuid, followUpDays = 27)

  private val appointmentConfig: AppointmentConfig = AppointmentConfig(
      appointmentDuePeriodForDefaulters = Period.ofDays(30),
      scheduleAppointmentsIn = listOf(Days(1)),
      defaultTimeToAppointment = Days(1),
      periodForIncludingOverdueAppointments = Period.ofMonths(12)
  )

  private lateinit var controller: ScheduleAppointmentSheetController

  @Test
  fun `when done is clicked, appointment should be scheduled with the correct due date`() {
    whenever(repository.schedule(any(), any(), any(), any(), any(), any())).thenReturn(Single.just(TestData.appointment()))

    val date = LocalDate.now(clock).plus(1, ChronoUnit.MONTHS)
    val defaultTimeToAppointment = Months(1)
    val periodsToScheduleAppointmentsIn = listOf(defaultTimeToAppointment)

    val protocol = TestData.protocol(protocolUuid, followUpDays = 31)
    sheetCreated(
        patientUuid = patientUuid,
        user = user,
        facility = facility,
        protocolUuid = protocol.uuid,
        protocol = Observable.just(protocol),
        config = appointmentConfig.withScheduledAppointments(periodsToScheduleAppointmentsIn)
    )

    uiEvents.onNext(AppointmentDone)

    verify(repository).schedule(eq(patientUuid), any(), eq(date), eq(Manual), eq(facility.uuid), eq(facility.uuid))
    verify(sheet).closeSheet()
  }

  @Test
  @Parameters(value = [
    "true, true",
    "false, false"]
  )
  fun `when scheduling an appointment is skipped and the patient is a defaulter, an automatic appointment should be scheduled`(
      isPatientDefaulter: Boolean,
      shouldAutomaticAppointmentBeScheduled: Boolean
  ) {
    whenever(patientRepository.isPatientDefaulter(patientUuid)).thenReturn(Observable.just(isPatientDefaulter))
    whenever(repository.schedule(any(), any(), any(), any(), any(), any())).thenReturn(Single.just(TestData.appointment()))

    sheetCreated(
        patientUuid = patientUuid,
        user = user,
        facility = facility,
        protocolUuid = protocol.uuid,
        protocol = Observable.just(protocol)
    )
    uiEvents.onNext(SchedulingSkipped)

    if (shouldAutomaticAppointmentBeScheduled) {
      verify(repository).schedule(
          patientUuid = eq(patientUuid),
          appointmentUuid = any(),
          appointmentDate = eq(LocalDate.now(clock).plus(Period.ofDays(30))),
          appointmentType = eq(Automatic),
          appointmentFacilityUuid = eq(facility.uuid),
          creationFacilityUuid = eq(facility.uuid)
      )
    } else {
      verify(repository, never()).schedule(any(), any(), any(), any(), any(), any())
    }
    verify(sheet).closeSheet()
  }

  @Test
  fun `when default appointment is set, then update scheduled appointment`() {
    val scheduleAppointmentsIn = listOf(
        Days(1),
        Days(2),
        Days(3)
    )

    val protocol = TestData.protocol(protocolUuid, followUpDays = 2)
    sheetCreated(
        patientUuid = patientUuid,
        user = user,
        facility = facility,
        protocolUuid = protocol.uuid,
        protocol = Observable.just(protocol),
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    verify(sheet).showPatientFacility(facility.name)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    verify(sheet).enableIncrementButton(true)
    verify(sheet).enableDecrementButton(true)

    verifyNoMoreInteractions(sheet)
  }

  @Test
  fun `when incremented, then next appointment in the list should be scheduled`() {
    // given
    val scheduleAppointmentsIn = listOf(
        Days(1),
        Days(2),
        Days(7)
    )

    // when
    val protocol = TestData.protocol(protocolUuid, followUpDays = 2)
    sheetCreated(
        patientUuid = patientUuid,
        user = user,
        facility = facility,
        protocolUuid = protocol.uuid,
        protocol = Observable.just(protocol),
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    // then
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(sheet)

    uiEvents.onNext(AppointmentDateIncremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Days(7))
    reset(sheet)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-04")))
    uiEvents.onNext(AppointmentDateIncremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Days(7))
  }

  @Test
  fun `when decremented, then previous appointment in the list should be scheduled`() {
    // given
    val scheduleAppointmentsIn = listOf(
        Days(1),
        Days(2),
        Days(7)
    )

    // when
    val protocol = TestData.protocol(protocolUuid, followUpDays = 2)
    sheetCreated(
        patientUuid = patientUuid,
        user = user,
        facility = facility,
        protocolUuid = protocol.uuid,
        protocol = Observable.just(protocol),
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    //then
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(sheet)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-02"), Days(1))
    reset(sheet)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-07")))
    uiEvents.onNext(AppointmentDateDecremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
  }

  @Test
  fun `when protocol is not provided, then config default follow up days must be set`() {
    // given
    val scheduleAppointmentsIn = listOf(
        Days(1),
        Days(2),
        Days(7)
    )

    // when
    val protocol = TestData.protocol(protocolUuid, followUpDays = 2)
    sheetCreated(
        patientUuid = patientUuid,
        user = user,
        facility = facility,
        protocolUuid = protocol.uuid,
        protocol = Observable.never(),
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    //then
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-02"), Days(1))
  }

  @Test
  fun `when date is selected and date is incremented, the nearest date should be chosen`() {
    // given
    val scheduleAppointmentsIn = listOf(
        Days(1),
        Days(2),
        Days(3),
        Days(4)
    )

    val protocol = TestData.protocol(protocolUuid, followUpDays = 2)
    sheetCreated(
        patientUuid = patientUuid,
        user = user,
        facility = facility,
        protocolUuid = protocol.uuid,
        protocol = Observable.just(protocol),
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    verify(sheet).enableIncrementButton(true)
    verify(sheet).enableDecrementButton(true)

    val threeDaysFromNow = LocalDate.now(clock).plusDays(3)
    val year = threeDaysFromNow.year
    val month = threeDaysFromNow.monthValue
    val dayOfMonth = threeDaysFromNow.dayOfMonth

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.of(year, month, dayOfMonth)))

    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-04"), Days(3))

    uiEvents.onNext(AppointmentDateIncremented)

    verify(sheet).showPatientFacility(facility.name)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-05"), Days(4))
    verify(sheet).enableIncrementButton(false)

    verifyNoMoreInteractions(sheet)
  }

  @Test
  fun `when the manually select appointment date button is clicked, the date picker must be shown set to the last selected appointment date`() {
    // given
    val scheduleAppointmentsIn = listOf(
        Days(1),
        Days(2),
        Weeks(1)
    )

    // when
    val protocol = TestData.protocol(protocolUuid, followUpDays = 2)
    sheetCreated(
        patientUuid = patientUuid,
        user = user,
        facility = facility,
        protocolUuid = protocol.uuid,
        protocol = Observable.just(protocol),
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    // then
    uiEvents.onNext(ManuallySelectAppointmentDateClicked)
    verify(sheet).showManualDateSelector(LocalDate.parse("2019-01-03"))
    reset(sheet)

    uiEvents.onNext(AppointmentDateIncremented)
    uiEvents.onNext(ManuallySelectAppointmentDateClicked)
    verify(sheet).showManualDateSelector(LocalDate.parse("2019-01-08"))
    reset(sheet)

    uiEvents.onNext(AppointmentDateDecremented)
    uiEvents.onNext(AppointmentDateDecremented)
    uiEvents.onNext(ManuallySelectAppointmentDateClicked)
    verify(sheet).showManualDateSelector(LocalDate.parse("2019-01-02"))
    reset(sheet)

    val lastSelectedCalendarDate = LocalDate.parse("2019-01-05")
    uiEvents.onNext(AppointmentCalendarDateSelected(lastSelectedCalendarDate))
    uiEvents.onNext(ManuallySelectAppointmentDateClicked)
    verify(sheet).showManualDateSelector(lastSelectedCalendarDate)
    reset(sheet)
  }

  @Test
  fun `duplicate configured periods for scheduling appointment dates must be ignored`() {
    // given
    val scheduleAppointmentsIn = listOf(
        Days(2),
        Days(2),
        Days(1),
        Days(7),
        Weeks(1),
        Days(7),
        Weeks(2),
        Days(14)
    )

    // when
    val protocol = TestData.protocol(protocolUuid, followUpDays = 1)
    sheetCreated(
        patientUuid = patientUuid,
        user = user,
        facility = facility,
        protocolUuid = protocol.uuid,
        protocol = Observable.just(protocol),
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    // then
    uiEvents.onNext(AppointmentDateIncremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(sheet)

    uiEvents.onNext(AppointmentDateIncremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Days(7))
    reset(sheet)

    uiEvents.onNext(AppointmentDateIncremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-15"), Weeks(2))
    verify(sheet).enableIncrementButton(false)
    reset(sheet)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Days(7))
    reset(sheet)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(sheet)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-02"), Days(1))
  }

  @Test
  fun `out of order configured periods for scheduling appointment dates must not affect the incrementing and decrementing of dates`() {
    // given
    val scheduleAppointmentsIn = listOf(
        Days(2),
        Days(7),
        Weeks(1),
        Days(1),
        Weeks(2),
        Days(14)
    )

    // when
    val protocol = TestData.protocol(protocolUuid, followUpDays = 1)
    sheetCreated(
        patientUuid = patientUuid,
        user = user,
        facility = facility,
        protocolUuid = protocol.uuid,
        protocol = Observable.just(protocol),
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    // then
    uiEvents.onNext(AppointmentDateIncremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(sheet)

    uiEvents.onNext(AppointmentDateIncremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Days(7))
    reset(sheet)

    uiEvents.onNext(AppointmentDateIncremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-15"), Weeks(2))
    reset(sheet)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Days(7))
    reset(sheet)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(sheet)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-02"), Days(1))
  }

  @Test
  fun `when an exact calendar date is selected, the exact time to appointment from the configured ones must be selected if it is an exact match`() {
    // given
    val scheduleAppointmentsIn = listOf(
        Days(1),
        Days(2),
        Days(3),
        Weeks(1),
        Weeks(2),
        Days(21),
        Months(2)
    )

    // when
    sheetCreated(
        patientUuid = patientUuid,
        user = user,
        facility = facility,
        protocolUuid = protocol.uuid,
        protocol = Observable.just(protocol),
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    // then
    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-02")))
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-02"), Days(1))
    reset(sheet)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-03")))
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(sheet)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-04")))
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-04"), Days(3))
    reset(sheet)


    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-06")))
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-06"), Days(5))
    reset(sheet)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-08")))
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Weeks(1))
    reset(sheet)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-13")))
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-13"), Days(12))
    reset(sheet)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-15")))
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-15"), Weeks(2))
    reset(sheet)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-22")))
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-22"), Days(21))
    reset(sheet)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-31")))
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-31"), Days(30))
    reset(sheet)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-02-01")))
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-02-01"), Days(31))
    reset(sheet)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-03-01")))
    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-03-01"), Months(2))
    reset(sheet)
  }

  @Test
  fun `when sheet is opened then user's current facility should be displayed`() {
    //when
    sheetCreated()

    //then
    verify(sheet).showPatientFacility(facility.name)
  }

  @Test
  fun `when patient facility is changed then appointment should be scheduled in the changed facility`() {
    //given
    val updatedFacilityUuid = TestData.facility().uuid
    val appointment = TestData.appointment()

    whenever(facilityRepository.facility(updatedFacilityUuid)).thenReturn(Just(TestData.facility(uuid = updatedFacilityUuid)))
    whenever(repository.schedule(any(), any(), any(), any(), any(), any())).thenReturn(Single.just(appointment))

    //when
    sheetCreated()
    uiEvents.onNext(PatientFacilityChanged(updatedFacilityUuid))
    uiEvents.onNext(AppointmentDone)

    //then
    verify(repository).schedule(eq(patientUuid), any(), any(), any(), eq(updatedFacilityUuid), eq(facility.uuid))
    verify(sheet).closeSheet()
  }

  @Test
  fun `when patient facility is not changed then appointment should be scheduled in the current facility`() {
    //given
    val appointment = TestData.appointment()

    whenever(repository.schedule(any(), any(), any(), any(), any(), any())).thenReturn(Single.just(appointment))

    //when
    sheetCreated()
    uiEvents.onNext(AppointmentDone)

    //then
    verify(repository).schedule(eq(patientUuid), any(), any(), any(), eq(facility.uuid), eq(facility.uuid))
    verify(sheet).closeSheet()
  }

  @Test
  fun `when patient facility is changed then show selected facility`() {
    //given
    val updatedFacilityUuid = UUID.fromString("60b32059-fe85-4b90-8a5d-984e56f9b001")
    val updatedFacility = TestData.facility(uuid = updatedFacilityUuid, name = "new facility")

    whenever(facilityRepository.facility(updatedFacilityUuid)).thenReturn(Just(updatedFacility))

    //when
    sheetCreated()
    uiEvents.onNext(PatientFacilityChanged(updatedFacilityUuid))

    //then
    val inOrder = inOrder(sheet)
    inOrder.verify(sheet).showPatientFacility(facility.name)
    inOrder.verify(sheet).showPatientFacility(updatedFacility.name)
  }


  private fun sheetCreated(
      patientUuid: UUID = this.patientUuid,
      user: User = this.user,
      facility: Facility = this.facility,
      protocolUuid: UUID = this.protocolUuid,
      protocol: Observable<Protocol> = Observable.just(this.protocol),
      config: AppointmentConfig = this.appointmentConfig
  ) {
    controller = ScheduleAppointmentSheetController(
        appointmentRepository = repository,
        patientRepository = patientRepository,
        config = config,
        clock = clock,
        userSession = userSession,
        facilityRepository = facilityRepository,
        protocolRepository = protocolRepository
    )

    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(facility))

    whenever(protocolRepository.protocol(protocolUuid)).thenReturn(protocol)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(sheet) }

    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid))
  }
}

private fun AppointmentConfig.withScheduledAppointments(scheduleAppointmentsIn: List<TimeToAppointment>): AppointmentConfig {
  return this.copy(scheduleAppointmentsIn = scheduleAppointmentsIn)
}
