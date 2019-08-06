package org.simple.clinic.scheduleappointment

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
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
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
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
  private val uuid = UUID.randomUUID()
  private val clock = TestUtcClock()
  private val configStream = PublishSubject.create<AppointmentConfig>()
  private val facility = PatientMocker.facility()
  private val user = PatientMocker.loggedInUser()
  private val userSubject = PublishSubject.create<User>()
  private val scheduledAppointmentConfigSubject = PublishSubject.create<ScheduleAppointmentConfig>()

  val controller = ScheduleAppointmentSheetController(
      config = scheduledAppointmentConfigSubject,
      appointmentRepository = repository,
      patientRepository = patientRepository,
      configProvider = configStream.firstOrError(),
      utcClock = clock,
      userSession = userSession,
      facilityRepository = facilityRepository
  )

  @Before
  fun setUp() {
    clock.setYear(2019)
    whenever(userSession.requireLoggedInUser()).thenReturn(userSubject)
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(facility))

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(sheet) }

    userSubject.onNext(user)
  }

  @Test
  fun `when done is clicked, appointment should be scheduled with the correct due date`() {
    whenever(repository.schedule(any(), any(), any(), any())).thenReturn(Single.just(PatientMocker.appointment()))

    val date = LocalDate.now(clock).plus(1, ChronoUnit.MONTHS)
    val oneMonth = ScheduleAppointment(displayText = "1 month", timeAmount = 1, chronoUnit = ChronoUnit.MONTHS)
    val possibleAppointments = listOf(oneMonth)

    scheduledAppointmentConfigSubject.onNext(ScheduleAppointmentConfig(
        possibleAppointments = possibleAppointments,
        defaultAppointment = oneMonth
    ))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = uuid))

    uiEvents.onNext(AppointmentDone)

    verify(repository).schedule(uuid, date, Manual, facility)
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
    whenever(patientRepository.isPatientDefaulter(uuid)).thenReturn(Observable.just(isPatientDefaulter))
    whenever(repository.schedule(any(), any(), any(), any())).thenReturn(Single.just(PatientMocker.appointment()))

    configStream.onNext(AppointmentConfig(
        minimumOverduePeriodForHighRisk = Period.ofDays(30),
        overduePeriodForLowestRiskLevel = Period.ofDays(365),
        appointmentDuePeriodForDefaulters = Period.ofDays(30)
    ))

    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = uuid))
    uiEvents.onNext(SchedulingSkipped)

    if (shouldAutomaticAppointmentBeScheduled) {
      verify(repository).schedule(
          patientUuid = uuid,
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
    val defaultAppointment = ScheduleAppointment("2 days", 2, ChronoUnit.DAYS)

    val possibleAppointments = listOf(
        ScheduleAppointment("1 day", 1, ChronoUnit.DAYS),
        defaultAppointment,
        ScheduleAppointment("3 days", 3, ChronoUnit.DAYS)
    )

    scheduledAppointmentConfigSubject.onNext(ScheduleAppointmentConfig(possibleAppointments, defaultAppointment))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = UUID.randomUUID()))

    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"))
    verify(sheet).enableIncrementButton(true)
    verify(sheet).enableDecrementButton(true)

    verifyNoMoreInteractions(sheet)
  }

  @Test
  fun `when incremented, then next appointment in the list should be scheduled`() {
    val defaultAppointment = ScheduleAppointment("2 days", 2, ChronoUnit.DAYS)
    val thirdAppointment = ScheduleAppointment("3 days", 3, ChronoUnit.DAYS)

    val possibleAppointments = listOf(
        ScheduleAppointment("1 day", 1, ChronoUnit.DAYS),
        defaultAppointment,
        thirdAppointment
    )

    scheduledAppointmentConfigSubject.onNext(ScheduleAppointmentConfig(possibleAppointments, defaultAppointment))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = UUID.randomUUID()))

    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"))
    verify(sheet).enableIncrementButton(true)
    verify(sheet).enableDecrementButton(true)

    uiEvents.onNext(AppointmentDateIncremented)

    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-04"))
    verify(sheet).enableIncrementButton(false)

    verifyNoMoreInteractions(sheet)
  }

  @Test
  fun `when decremented, then previous appointment in the list should be scheduled`() {
    val firstAppointment = ScheduleAppointment("1 day", 1, ChronoUnit.DAYS)
    val defaultAppointment = ScheduleAppointment("2 days", 2, ChronoUnit.DAYS)

    val possibleAppointments = listOf(
        firstAppointment,
        defaultAppointment,
        ScheduleAppointment("3 days", 3, ChronoUnit.DAYS)
    )

    scheduledAppointmentConfigSubject.onNext(ScheduleAppointmentConfig(possibleAppointments, defaultAppointment))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = UUID.randomUUID()))

    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"))
    verify(sheet).enableIncrementButton(true)
    verify(sheet).enableDecrementButton(true)

    uiEvents.onNext(AppointmentDateDecremented)

    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-02"))
    verify(sheet).enableDecrementButton(false)

    verifyNoMoreInteractions(sheet)
  }

  @Test
  fun `when date is selected and date is incremented, the nearest date should be chosen`() {
    val twoDaysAppointment = ScheduleAppointment("2 days", 2, ChronoUnit.DAYS)

    val possibleAppointments = listOf(
        ScheduleAppointment("1 day", 1, ChronoUnit.DAYS),
        twoDaysAppointment,
        ScheduleAppointment("3 days", 3, ChronoUnit.DAYS),
        ScheduleAppointment("4 days", 4, ChronoUnit.DAYS)
    )

    scheduledAppointmentConfigSubject.onNext(ScheduleAppointmentConfig(possibleAppointments, twoDaysAppointment))
    uiEvents.onNext(ScheduleAppointmentSheetCreated(patientUuid = UUID.randomUUID()))

    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-03"))
    verify(sheet).enableIncrementButton(true)
    verify(sheet).enableDecrementButton(true)

    val threeDaysFromNow = LocalDate.now(clock).plusDays(3)
    val year = threeDaysFromNow.year
    val month = threeDaysFromNow.monthValue
    val dayOfMonth = threeDaysFromNow.dayOfMonth

    uiEvents.onNext(AppointmentCalendarDateSelected(
        year = year,
        month = month,
        dayOfMonth = dayOfMonth
    ))

    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-04"))

    uiEvents.onNext(AppointmentDateIncremented)

    verify(sheet).updateScheduledAppointment(LocalDate.parse("2019-01-05"))
    verify(sheet).enableIncrementButton(false)

    verifyNoMoreInteractions(sheet)
  }
}
