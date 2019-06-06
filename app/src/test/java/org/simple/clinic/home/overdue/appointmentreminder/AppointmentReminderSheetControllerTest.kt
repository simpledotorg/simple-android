package org.simple.clinic.home.overdue.appointmentreminder

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class AppointmentReminderSheetControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val sheet = mock<AppointmentReminderSheet>()
  private val repository = mock<AppointmentRepository>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val appointmentUuid = UUID.randomUUID()
  val user = PatientMocker.loggedInUser()
  val userSubject = PublishSubject.create<User>()
  val userSession = mock<UserSession>()

  val controller = AppointmentReminderSheetController(repository, userSession)

  @Before
  fun setUp() {
    whenever(userSession.requireLoggedInUser()).thenReturn(userSubject)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(sheet) }

    userSubject.onNext(user)
  }

  @Test
  fun `when sheet is created, a date should immediately be displayed to the user`() {
    val current = 17
    uiEvents.onNext(AppointmentReminderSheetCreated(current, appointmentUuid))

    verify(sheet).updateDisplayedDate(current)
  }

  @Test
  @Parameters(method = "paramsForIncrementingAndDecrementing")
  fun `when increment button is clicked, appointment due date should increase`(
      current: Int,
      size: Int
  ) {
    uiEvents.onNext(ReminderDateIncremented(current, size))

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
      verify(sheet, never()).updateDisplayedDate(any())
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
    uiEvents.onNext(ReminderDateDecremented(current, size))

    if (current == 0) {
      verify(sheet, never()).updateDisplayedDate(any())
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
    whenever(repository.createReminder(any(), any())).thenReturn(Completable.complete())

    val current = AppointmentReminder("2 weeks", 2, ChronoUnit.WEEKS)
    uiEvents.onNext(AppointmentReminderSheetCreated(3, appointmentUuid))
    uiEvents.onNext(ReminderCreated(current))

    verify(repository).createReminder(appointmentUuid, LocalDate.now(ZoneOffset.UTC).plus(2L, ChronoUnit.WEEKS))
    verify(sheet).closeSheet()
  }

  @Test
  @Parameters(value = [
    "NOT_LOGGED_IN|false",
    "OTP_REQUESTED|false",
    "LOGGED_IN|false",
    "RESETTING_PIN|false",
    "RESET_PIN_REQUESTED|false",
    "UNAUTHORIZED|true"
  ])
  fun `whenever the user status becomes unauthorized, then close the sheet`(
      loggedInStatus: User.LoggedInStatus,
      shouldCloseSheet: Boolean
  ) {
    verify(sheet, never()).finish()

    userSubject.onNext(user.copy(loggedInStatus = loggedInStatus))

    if(shouldCloseSheet) {
      verify(sheet).finish()
    } else {
      verify(sheet, never()).finish()
    }
  }
}

