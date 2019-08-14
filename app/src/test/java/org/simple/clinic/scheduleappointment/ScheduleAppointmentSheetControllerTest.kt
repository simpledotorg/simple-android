package org.simple.clinic.scheduleappointment

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.Appointment.AppointmentType.Automatic
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.scheduleappointment.TimeToAppointment.Days
import org.simple.clinic.scheduleappointment.TimeToAppointment.Months
import org.simple.clinic.scheduleappointment.TimeToAppointment.Weeks
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
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
  private val userSession = mock<UserSession>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val today = LocalDate.parse("2019-01-01")
  private val clock = TestUserClock(today)
  private val configStream = PublishSubject.create<AppointmentConfig>()
  private val facility = PatientMocker.facility()
  private val user = PatientMocker.loggedInUser()
  private val userSubject = PublishSubject.create<User>()
  private val patientUuid = UUID.fromString("d44bf81f-4369-4bbc-a51b-52d88c54f065")

  private val appointmentConfig: AppointmentConfig = AppointmentConfig(
      minimumOverduePeriodForHighRisk = Period.ofDays(30),
      overduePeriodForLowestRiskLevel = Period.ofDays(365),
      appointmentDuePeriodForDefaulters = Period.ofDays(30),
      scheduleAppointmentsIn = listOf(Days(1)),
      defaultTimeToAppointment = Days(1)
  )

  val controller = ScheduleAppointmentSheetController(
      appointmentRepository = repository,
      patientRepository = patientRepository,
      configProvider = configStream,
      clock = clock,
      userSession = userSession,
      facilityRepository = facilityRepository
  )

  @Before
  fun setUp() {
    whenever(userSession.requireLoggedInUser()).thenReturn(userSubject)
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(facility))

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(sheet) }

    userSubject.onNext(user)
  }

  @Test
  fun `when done is clicked, appointment should be scheduled with the correct due date`() {
    whenever(repository.schedule(any(), any(), any(), any())).thenReturn(Single.just(PatientMocker.appointment()))

    val date = LocalDate.now(clock).plus(1, ChronoUnit.MONTHS)
    val defaultTimeToAppointment = Months(1)
    val periodsToScheduleAppointmentsIn = listOf(defaultTimeToAppointment)

    configStream.onNext(appointmentConfig.withScheduledAppointments(periodsToScheduleAppointmentsIn, defaultTimeToAppointment))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = patientUuid))

    uiEvents.onNext(AppointmentDone)

    verify(repository).schedule(patientUuid, date, Manual, facility)
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
    whenever(repository.schedule(any(), any(), any(), any())).thenReturn(Single.just(PatientMocker.appointment()))

    configStream.onNext(appointmentConfig)
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = patientUuid))
    uiEvents.onNext(SchedulingSkipped)

    if (shouldAutomaticAppointmentBeScheduled) {
      verify(repository).schedule(
          patientUuid = patientUuid,
          appointmentDate = LocalDate.now(clock).plus(Period.ofDays(30)),
          appointmentType = Automatic,
          currentFacility = facility
      )
    } else {
      verify(repository, never()).schedule(any(), any(), any(), any())
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

    configStream.onNext(appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn, Days(2)))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = patientUuid))

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
    val defaultTimeToAppointment = Days(2)

    // when
    configStream.onNext(appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn, defaultTimeToAppointment))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = patientUuid))

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
    val defaultTimeToAppointment = Days(2)

    // when
    configStream.onNext(appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn, defaultTimeToAppointment))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = patientUuid))

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
  fun `when date is selected and date is incremented, the nearest date should be chosen`() {
    // given
    val scheduleAppointmentsIn = listOf(
        Days(1),
        Days(2),
        Days(3),
        Days(4)
    )
    val defaultTimeToAppointment = Days(2)

    configStream.onNext(appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn, defaultTimeToAppointment))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = patientUuid))

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
    val scheduleAppointmentInByDefault = Days(2)

    // when
    configStream.onNext(appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn, scheduleAppointmentInByDefault))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = patientUuid))

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
    val defaultTimeToAppointment = Days(1)

    // when
    configStream.onNext(appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn, defaultTimeToAppointment))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = patientUuid))

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
    val defaultTimeToAppointment = Days(1)

    // when
    configStream.onNext(appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn, defaultTimeToAppointment))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = patientUuid))

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
    val defaultTimeToAppointment = Days(2)

    // when
    configStream.onNext(appointmentConfig.withScheduledAppointments(scheduleAppointmentsIn, defaultTimeToAppointment))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = patientUuid))

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
}

private fun AppointmentConfig.withScheduledAppointments(
    scheduleAppointmentsIn: List<TimeToAppointment>,
    defaultTimeToAppointment: TimeToAppointment
): AppointmentConfig {
  return this.copy(
      scheduleAppointmentsIn = scheduleAppointmentsIn,
      defaultTimeToAppointment = defaultTimeToAppointment
  )
}
