package org.simple.clinic.scheduleappointment

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.Appointment.AppointmentType.Automatic
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.overdue.TimeToAppointment.Days
import org.simple.clinic.overdue.TimeToAppointment.Months
import org.simple.clinic.overdue.TimeToAppointment.Weeks
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.protocol.Protocol
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.util.Just
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import java.util.UUID

class ScheduleAppointmentLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<ScheduleAppointmentUi>()
  private val uiActions = mock<ScheduleAppointmentUiActions>()
  private val repository = mock<AppointmentRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val protocolRepository = mock<ProtocolRepository>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val today = LocalDate.parse("2019-01-01")
  private val clock = TestUserClock(today)
  private val patientUuid = UUID.fromString("d44bf81f-4369-4bbc-a51b-52d88c54f065")
  private val protocolUuid = UUID.fromString("8782e890-2fb0-4204-8647-6d20006cec02")
  private val facility = TestData.facility(protocolUuid = protocolUuid)
  private val protocol = TestData.protocol(protocolUuid, followUpDays = 27)

  private val appointmentConfig: AppointmentConfig = AppointmentConfig(
      appointmentDuePeriodForDefaulters = Period.ofDays(30),
      scheduleAppointmentsIn = listOf(Days(1)),
      defaultTimeToAppointment = Days(1),
      periodForIncludingOverdueAppointments = Period.ofMonths(12),
      remindAppointmentsIn = emptyList()
  )

  private lateinit var testFixture: MobiusTestFixture<ScheduleAppointmentModel, ScheduleAppointmentEvent, ScheduleAppointmentEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when done is clicked, appointment should be scheduled with the correct due date`() {
    val defaultTimeToAppointment = Months(1)
    val periodsToScheduleAppointmentsIn = listOf(defaultTimeToAppointment)
    val scheduledDate = LocalDate.parse("2019-01-28")

    sheetCreated(config = appointmentConfig.withScheduledAppointments(periodsToScheduleAppointmentsIn))

    uiEvents.onNext(AppointmentDone)

    verify(ui).showPatientFacility(facility.name)
    verify(ui).enableIncrementButton(true)
    verify(ui).enableDecrementButton(false)
    verify(ui).updateScheduledAppointment(scheduledDate, Days(protocol.followUpDays))
    verifyNoMoreInteractions(ui)

    verify(uiActions).closeSheet()
    verifyNoMoreInteractions(uiActions)

    verify(repository).schedule(
        patientUuid = eq(patientUuid),
        appointmentUuid = any(),
        appointmentDate = eq(scheduledDate),
        appointmentType = eq(Manual),
        appointmentFacilityUuid = eq(facility.uuid),
        creationFacilityUuid = eq(facility.uuid)
    )
  }

  @Test
  fun `when scheduling an appointment is skipped and the patient is a defaulter, an automatic appointment should be scheduled`() {
    whenever(patientRepository.isPatientDefaulter(patientUuid)).thenReturn(Observable.just(true))
    val scheduledDate = LocalDate.parse("2019-01-31")

    sheetCreated()
    uiEvents.onNext(SchedulingSkipped)

    verify(ui).showPatientFacility(facility.name)
    verify(ui).enableIncrementButton(false)
    verify(ui).enableDecrementButton(true)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-28"), Days(protocol.followUpDays))
    verifyNoMoreInteractions(ui)

    verify(uiActions).closeSheet()
    verifyNoMoreInteractions(uiActions)

    verify(repository).schedule(
        patientUuid = eq(patientUuid),
        appointmentUuid = any(),
        appointmentDate = eq(scheduledDate),
        appointmentType = eq(Automatic),
        appointmentFacilityUuid = eq(facility.uuid),
        creationFacilityUuid = eq(facility.uuid)
    )
  }

  @Test
  fun `when scheduling an appointment is skipped and the patient is not a defaulter, an automatic appointment should not be scheduled`() {
    whenever(patientRepository.isPatientDefaulter(patientUuid)).thenReturn(Observable.just(false))

    sheetCreated()
    uiEvents.onNext(SchedulingSkipped)

    verify(repository, never()).schedule(any(), any(), any(), any(), any(), any())
    verify(ui).showPatientFacility(facility.name)
    verify(ui).enableIncrementButton(false)
    verify(ui).enableDecrementButton(true)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-28"), Days(protocol.followUpDays))
    verifyNoMoreInteractions(ui)

    verify(uiActions).closeSheet()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `the initial appointment date must be selected from the protocol`() {
    val scheduleAppointmentsIn = listOf(
        Days(1),
        Days(2),
        Days(3)
    )

    val protocol = TestData.protocol(protocolUuid, followUpDays = 2)
    sheetCreated(
        protocol = protocol,
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    verify(ui).showPatientFacility(facility.name)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    verify(ui).enableIncrementButton(true)
    verify(ui).enableDecrementButton(true)
    verifyNoMoreInteractions(ui)

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when there is no protocol for the facility, the initial appointment date must be selected from the default value`() {
    // given
    val scheduleAppointmentsIn = listOf(
        Days(1),
        Days(2),
        Days(7)
    )

    // when
    val defaultTimeToAppointment = Days(2)
    sheetCreatedWithoutProtocol(
        config = appointmentConfig
            .withScheduledAppointments(scheduleAppointmentsIn)
            .withDefaultTimeToAppointment(defaultTimeToAppointment)
    )

    //then
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-03"), defaultTimeToAppointment)
    verify(ui).showPatientFacility(facility.name)
    verify(ui).enableIncrementButton(true)
    verify(ui).enableDecrementButton(true)
    verifyNoMoreInteractions(ui)

    verifyZeroInteractions(uiActions)
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
        protocol = protocol,
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    // then
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(ui)

    uiEvents.onNext(AppointmentDateIncremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Days(7))
    reset(ui)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-04")))
    uiEvents.onNext(AppointmentDateIncremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Days(7))

    verifyZeroInteractions(uiActions)
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
        protocol = protocol,
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    //then
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(ui)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-02"), Days(1))
    reset(ui)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-07")))
    uiEvents.onNext(AppointmentDateDecremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))

    verifyZeroInteractions(uiActions)
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
        protocol = protocol,
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    verify(ui).showPatientFacility(facility.name)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    verify(ui).enableIncrementButton(true)
    verify(ui).enableDecrementButton(true)
    verifyNoMoreInteractions(ui)

    val threeDaysFromNow = LocalDate.now(clock).plusDays(3)
    val year = threeDaysFromNow.year
    val month = threeDaysFromNow.monthValue
    val dayOfMonth = threeDaysFromNow.dayOfMonth

    reset(ui)
    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.of(year, month, dayOfMonth)))
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-04"), Days(3))
    verify(ui).enableIncrementButton(true)
    verify(ui).enableDecrementButton(true)
    verifyNoMoreInteractions(ui)

    reset(ui)
    uiEvents.onNext(AppointmentDateIncremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-05"), Days(4))
    verify(ui).enableIncrementButton(false)
    verify(ui).enableDecrementButton(true)
    verifyNoMoreInteractions(ui)

    verifyZeroInteractions(uiActions)
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
        protocol = protocol,
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    // then
    uiEvents.onNext(ManuallySelectAppointmentDateClicked)
    verify(uiActions).showManualDateSelector(LocalDate.parse("2019-01-03"))
    reset(uiActions)

    uiEvents.onNext(AppointmentDateIncremented)
    uiEvents.onNext(ManuallySelectAppointmentDateClicked)
    verify(uiActions).showManualDateSelector(LocalDate.parse("2019-01-08"))
    reset(uiActions)

    uiEvents.onNext(AppointmentDateDecremented)
    uiEvents.onNext(AppointmentDateDecremented)
    uiEvents.onNext(ManuallySelectAppointmentDateClicked)
    verify(uiActions).showManualDateSelector(LocalDate.parse("2019-01-02"))
    reset(uiActions)

    val lastSelectedCalendarDate = LocalDate.parse("2019-01-05")
    uiEvents.onNext(AppointmentCalendarDateSelected(lastSelectedCalendarDate))
    uiEvents.onNext(ManuallySelectAppointmentDateClicked)
    verify(uiActions).showManualDateSelector(lastSelectedCalendarDate)
    reset(uiActions)
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
        protocol = protocol,
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    // then
    uiEvents.onNext(AppointmentDateIncremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(ui)

    uiEvents.onNext(AppointmentDateIncremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Days(7))
    reset(ui)

    uiEvents.onNext(AppointmentDateIncremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-15"), Weeks(2))
    verify(ui).enableIncrementButton(false)
    reset(ui)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Days(7))
    reset(ui)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(ui)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-02"), Days(1))

    verifyZeroInteractions(uiActions)
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
        protocol = protocol,
        config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn)
    )

    // then
    uiEvents.onNext(AppointmentDateIncremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(ui)

    uiEvents.onNext(AppointmentDateIncremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Days(7))
    reset(ui)

    uiEvents.onNext(AppointmentDateIncremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-15"), Weeks(2))
    reset(ui)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Days(7))
    reset(ui)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(ui)

    uiEvents.onNext(AppointmentDateDecremented)
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-02"), Days(1))

    verifyZeroInteractions(uiActions)
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
    sheetCreated(config = appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn))

    // then
    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-02")))
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-02"), Days(1))
    reset(ui)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-03")))
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-03"), Days(2))
    reset(ui)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-04")))
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-04"), Days(3))
    reset(ui)


    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-06")))
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-06"), Days(5))
    reset(ui)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-08")))
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-08"), Weeks(1))
    reset(ui)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-13")))
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-13"), Days(12))
    reset(ui)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-15")))
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-15"), Weeks(2))
    reset(ui)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-22")))
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-22"), Days(21))
    reset(ui)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-01-31")))
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-01-31"), Days(30))
    reset(ui)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-02-01")))
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-02-01"), Days(31))
    reset(ui)

    uiEvents.onNext(AppointmentCalendarDateSelected(LocalDate.parse("2019-03-01")))
    verify(ui).updateScheduledAppointment(LocalDate.parse("2019-03-01"), Months(2))
    reset(ui)

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when sheet is opened then user's current facility should be displayed`() {
    //when
    sheetCreated()

    //then
    verify(ui).showPatientFacility(facility.name)
  }

  @Test
  fun `when patient facility is changed then appointment should be scheduled in the changed facility`() {
    //given
    val updatedFacility = TestData.facility(uuid = UUID.fromString("2bb13dc3-305e-4bb2-bea7-ea853acf47cb"))
    val updatedFacilityUuid = updatedFacility.uuid
    val date = LocalDate.parse("2019-01-28")

    whenever(facilityRepository.facility(updatedFacilityUuid)).thenReturn(Just(updatedFacility))

    //when
    sheetCreated()
    uiEvents.onNext(PatientFacilityChanged(updatedFacility))
    uiEvents.onNext(AppointmentDone)

    //then
    verify(uiActions).closeSheet()
    verifyNoMoreInteractions(uiActions)

    verify(repository).schedule(
        patientUuid = eq(patientUuid),
        appointmentUuid = any(),
        appointmentDate = eq(date),
        appointmentType = eq(Manual),
        appointmentFacilityUuid = eq(updatedFacilityUuid),
        creationFacilityUuid = eq(facility.uuid)
    )
  }

  @Test
  fun `when patient facility is not changed then appointment should be scheduled in the current facility`() {
    //given
    val appointment = TestData.appointment()

    //when
    sheetCreated()
    uiEvents.onNext(AppointmentDone)

    //then
    verify(uiActions).closeSheet()
    verifyNoMoreInteractions(uiActions)

    verify(repository).schedule(
        patientUuid = eq(patientUuid),
        appointmentUuid = any(),
        appointmentDate = eq(LocalDate.parse("2019-01-28")),
        appointmentType = eq(Manual),
        appointmentFacilityUuid = eq(facility.uuid),
        creationFacilityUuid = eq(facility.uuid)
    )
  }

  @Test
  fun `when patient facility is changed then show selected facility`() {
    //given
    val updatedFacilityUuid = UUID.fromString("60b32059-fe85-4b90-8a5d-984e56f9b001")
    val updatedFacility = TestData.facility(uuid = updatedFacilityUuid, name = "new facility")

    whenever(facilityRepository.facility(updatedFacilityUuid)).thenReturn(Just(updatedFacility))

    //when
    sheetCreated()
    uiEvents.onNext(PatientFacilityChanged(updatedFacility))

    //then
    val inOrder = inOrder(ui)
    inOrder.verify(ui).showPatientFacility(facility.name)
    inOrder.verify(ui).showPatientFacility(updatedFacility.name)

    verifyZeroInteractions(uiActions)
  }

  private fun sheetCreated(
      facility: Facility = this.facility,
      protocol: Protocol = this.protocol,
      config: AppointmentConfig = this.appointmentConfig
  ) {
    setupMobiusTestFixture(facility, config)

    whenever(protocolRepository.protocol(protocol.uuid)).thenReturn(Observable.just(protocol))
    whenever(protocolRepository.protocolImmediate(protocol.uuid)).thenReturn(protocol)

    testFixture.start()
  }

  private fun sheetCreatedWithoutProtocol(
      facility: Facility = this.facility,
      config: AppointmentConfig = this.appointmentConfig
  ) {
    setupMobiusTestFixture(facility, config)

    whenever(protocolRepository.protocol(protocolUuid)).thenReturn(Observable.never())
    whenever(protocolRepository.protocolImmediate(protocolUuid)).thenReturn(null)

    testFixture.start()
  }

  private fun setupMobiusTestFixture(facility: Facility, config: AppointmentConfig) {
    val uiRenderer = ScheduleAppointmentUiRenderer(ui)

    val effectHandler = ScheduleAppointmentEffectHandler(
        currentFacility = Lazy { facility },
        protocolRepository = protocolRepository,
        appointmentRepository = repository,
        patientRepository = patientRepository,
        appointmentConfig = config,
        userClock = clock,
        schedulers = TrampolineSchedulersProvider(),
        uiActions = uiActions
    )

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = ScheduleAppointmentModel.create(
            patientUuid = patientUuid,
            timeToAppointments = config.scheduleAppointmentsIn,
            userClock = clock
        ),
        init = ScheduleAppointmentInit(),
        update = ScheduleAppointmentUpdate(
            currentDate = LocalDate.now(clock),
            defaulterAppointmentPeriod = config.appointmentDuePeriodForDefaulters
        ),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )
  }
}

private fun AppointmentConfig.withScheduledAppointments(scheduleAppointmentsIn: List<TimeToAppointment>): AppointmentConfig {
  return this.copy(scheduleAppointmentsIn = scheduleAppointmentsIn)
}

private fun AppointmentConfig.withDefaultTimeToAppointment(timeToAppointment: TimeToAppointment): AppointmentConfig {
  return copy(defaultTimeToAppointment = timeToAppointment)
}
