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
  private val utcClock = TestUtcClock()
  private val configStream = PublishSubject.create<AppointmentConfig>()
  private val facility = PatientMocker.facility()
  private val user = PatientMocker.loggedInUser()
  private val userSubject = PublishSubject.create<User>()

  val controller = ScheduleAppointmentSheetController(
      appointmentRepository = repository,
      patientRepository = patientRepository,
      configProvider = configStream.firstOrError(),
      utcClock = utcClock,
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
  fun `when sheet is created, a date should immediately be displayed to the user`() {
    val current = 17
    uiEvents.onNext(ScheduleAppointmentSheetCreated(current, uuid, 20))

    verify(sheet).updateDisplayedDate(current)
  }

  @Test
  @Parameters(method = "paramsForIncrementingAndDecrementing")
  fun `when increment button is clicked, appointment due date should increase`(
      current: Int,
      size: Int
  ) {
    uiEvents.onNext(AppointmentDateIncremented(current, size))

    if (current == 0) {
      verify(sheet).updateDisplayedDate(current + 1)
      verify(sheet).enableDecrementButton(true)
    }

    if (current != size - 2) {
      verify(sheet).updateDisplayedDate(current + 1)
      verify(sheet).enableIncrementButton(true)
    }

    if (current == size - 2) {
      verify(sheet).updateDisplayedDate(current + 1)
      verify(sheet).enableIncrementButton(false)
    }

    if (current == size - 1) {
      verify(sheet, never()).updateScheduledAppointment(any())
      verify(sheet, never()).enableIncrementButton(any())
      verify(sheet, never()).enableDecrementButton(any())
    }
  }

  @Test
  @Parameters(method = "paramsForIncrementingAndDecrementing")
  fun `when decrement button is clicked, appointment due date should decrease`(
      current: Int,
      size: Int
  ) {
    uiEvents.onNext(AppointmentDateDecremented(current, size))

    if (current == 0) {
      verify(sheet, never()).updateScheduledAppointment(any())
      verify(sheet, never()).enableIncrementButton(any())
      verify(sheet, never()).enableDecrementButton(any())
    }

    if (current == 1) {
      verify(sheet).updateDisplayedDate(current - 1)
      verify(sheet).enableDecrementButton(false)
    }

    if (current != 0) {
      verify(sheet).updateDisplayedDate(current - 1)
    }

    if (current == size - 1) {
      verify(sheet).updateDisplayedDate(current - 1)
      verify(sheet).enableIncrementButton(true)
    }
  }

  fun paramsForIncrementingAndDecrementing() = arrayOf(
      arrayOf(3, 9),
      arrayOf(1, 9),
      arrayOf(7, 9),
      arrayOf(2, 9)
  )

  @Test
  fun `when done is clicked, appointment should be scheduled with the correct due date`() {
    whenever(repository.schedule(any(), any(), any(), any())).thenReturn(Single.just(PatientMocker.appointment()))

    val current = ScheduleAppointment("1 month", 1, ChronoUnit.MONTHS)
    val date = LocalDate.now(utcClock).plus(1, ChronoUnit.MONTHS)
    uiEvents.onNext(ScheduleAppointmentSheetCreated(3, uuid, 4))
    uiEvents.onNext(AppointmentScheduled(current))

    verify(repository).schedule(uuid, date, Manual, facility)
    verify(sheet).closeSheet()
  }

  @Test
  fun `when last appointment date is chosen on sheet creation, then increment button should be disabled`() {
    uiEvents.onNext(ScheduleAppointmentSheetCreated(
        defaultDateIndex = 3,
        patientUuid = uuid,
        numberOfDates = 4
    ))

    verify(sheet).enableIncrementButton(false)
  }

  @Test
  fun `when not-last appointment date is chosen on sheet creation, then increment button should be enabled`() {
    uiEvents.onNext(ScheduleAppointmentSheetCreated(
        defaultDateIndex = 2,
        patientUuid = uuid,
        numberOfDates = 4
    ))

    verify(sheet).enableIncrementButton(true)
  }

  @Test
  fun `when first appointment date is chosen on sheet creation, then decrement button should be disabled`() {
    uiEvents.onNext(ScheduleAppointmentSheetCreated(
        defaultDateIndex = 0,
        patientUuid = uuid,
        numberOfDates = 4
    ))

    verify(sheet).enableDecrementButton(false)
  }

  @Test
  fun `when not-first appointment date is chosen on sheet creation, then decrement button should be enabled`() {
    uiEvents.onNext(ScheduleAppointmentSheetCreated(
        defaultDateIndex = 1,
        patientUuid = uuid,
        numberOfDates = 4
    ))

    verify(sheet).enableDecrementButton(true)
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
    uiEvents.onNext(ScheduleAppointmentSheetCreated(
        defaultDateIndex = 3,
        patientUuid = uuid,
        numberOfDates = 4
    ))
    uiEvents.onNext(SchedulingSkipped())

    if (shouldAutomaticAppointmentBeScheduled) {
      verify(repository).schedule(
          patientUuid = uuid,
          appointmentDate = LocalDate.now(utcClock).plus(Period.ofDays(30)),
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

    uiEvents.onNext(ScheduleAppointmentSheetCreated2(
        possibleAppointments = possibleAppointments,
        defaultAppointment = defaultAppointment
    ))

    verify(sheet).updateScheduledAppointment(defaultAppointment)
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

    uiEvents.onNext(ScheduleAppointmentSheetCreated2(
        possibleAppointments = possibleAppointments,
        defaultAppointment = defaultAppointment
    ))
    uiEvents.onNext(AppointmentDateIncremented2)

    verify(sheet).updateScheduledAppointment(defaultAppointment)
    verify(sheet).enableIncrementButton(true)
    verify(sheet).enableDecrementButton(true)

    uiEvents.onNext(AppointmentDateIncremented2)

    verify(sheet).updateScheduledAppointment(thirdAppointment)
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

    uiEvents.onNext(ScheduleAppointmentSheetCreated2(
        possibleAppointments = possibleAppointments,
        defaultAppointment = defaultAppointment
    ))

    verify(sheet).updateScheduledAppointment(defaultAppointment)
    verify(sheet).enableIncrementButton(true)
    verify(sheet).enableDecrementButton(true)

    uiEvents.onNext(AppointmentDateDecremented2)

    verify(sheet).updateScheduledAppointment(firstAppointment)
    verify(sheet).enableDecrementButton(false)

    verifyNoMoreInteractions(sheet)
  }

  @Test
  fun `when date is selected and date is incremented, the nearest date should be chosen`() {
    val twoDaysAppointment = ScheduleAppointment("2 days", 2, ChronoUnit.DAYS)

    val possibleAppointments = listOf(
        ScheduleAppointment("1 day", 1, ChronoUnit.DAYS),
        twoDaysAppointment,
        ScheduleAppointment("3 days", 3, ChronoUnit.DAYS)
    )

    uiEvents.onNext(ScheduleAppointmentSheetCreated2(possibleAppointments, twoDaysAppointment))

    verify(sheet).updateScheduledAppointment(ScheduleAppointment("2 days", 2, ChronoUnit.DAYS))
    verify(sheet).enableIncrementButton(true)
    verify(sheet).enableDecrementButton(true)

    val threeDaysFromNow = LocalDate.now(utcClock).plusDays(3)
    val year = threeDaysFromNow.year
    val month = threeDaysFromNow.monthValue
    val dayOfMonth = threeDaysFromNow.dayOfMonth

    uiEvents.onNext(AppointmentCalendarDateSelected(
        year = year,
        month = month,
        dayOfMonth = dayOfMonth
    ))

    verify(sheet).updateScheduledAppointment(ScheduleAppointment("3 days", 3, ChronoUnit.DAYS))

    verifyNoMoreInteractions(sheet)
  }
}
