package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import java.net.SocketTimeoutException

@RunWith(JUnitParamsRunner::class)
class PatientsScreenControllerTest {

  private val screen: PatientsScreen = mock()
  private val userSession = mock<UserSession>()
  private val approvalStatusApprovedAt = mock<Preference<Instant>>()

  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()
  private lateinit var controller: PatientsScreenController

  @Before
  fun setUp() {
    controller = PatientsScreenController(userSession, approvalStatusApprovedAt)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when new patient is clicked then patient search screen should open`() {
    uiEvents.onNext(NewPatientClicked())

    verify(screen).openNewPatientScreen()
  }

  @Test
  fun `when screen is created and the user is awaiting approval then the user's status should be checked`() {
    val user = PatientMocker.loggedInUser(status = UserStatus.WAITING_FOR_APPROVAL)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.complete())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(TheActivityLifecycle.Resumed())
    uiEvents.onNext(TheActivityLifecycle.Resumed())

    verify(userSession, times(3)).refreshLoggedInUser()
  }

  @Test
  @Parameters(value = ["APPROVED_FOR_SYNCING", "DISAPPROVED_FOR_SYNCING"])
  fun `when screen is created and the user is not awaiting approval then the user's status should not be checked`(
      status: UserStatus
  ) {
    val user = PatientMocker.loggedInUser(status = status)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.never())
    whenever(approvalStatusApprovedAt.get()).thenReturn(Instant.now())

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(TheActivityLifecycle.Resumed())
    uiEvents.onNext(TheActivityLifecycle.Resumed())

    verify(userSession, never()).refreshLoggedInUser()
  }

  @Test
  fun `when the user is waiting for awaiting approval then it's status should be shown`() {
    val user = PatientMocker.loggedInUser(status = UserStatus.WAITING_FOR_APPROVAL)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.never())

    uiEvents.onNext(ScreenCreated())

    verify(screen).showUserStatusAsWaiting()
  }

  @Test
  fun `when the user has been disapproved then the approval status shouldn't be shown`() {
    val user = PatientMocker.loggedInUser(status = UserStatus.DISAPPROVED_FOR_SYNCING)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))

    uiEvents.onNext(ScreenCreated())

    verify(screen).hideUserApprovalStatus()
  }

  @Test
  fun `when the user has been approved within the last 24h then the approval status should be shown`() {
    val user = PatientMocker.loggedInUser(status = UserStatus.APPROVED_FOR_SYNCING)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.complete())
    whenever(approvalStatusApprovedAt.get()).thenReturn(Instant.now().minus(23, ChronoUnit.HOURS))

    uiEvents.onNext(ScreenCreated())

    verify(screen).showUserStatusAsApproved()
  }

  @Test
  fun `when the user was approved earlier than 24h then the approval status should not be shown`() {
    val user = PatientMocker.loggedInUser(status = UserStatus.APPROVED_FOR_SYNCING)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.complete())
    whenever(approvalStatusApprovedAt.get()).thenReturn(Instant.now().minus(25, ChronoUnit.HOURS))

    uiEvents.onNext(ScreenCreated())

    verify(screen, never()).showUserStatusAsApproved()
  }

  @Test
  fun `when checking the user's status fails with any error then the error should be silently ignored`() {
    val user = PatientMocker.loggedInUser(status = UserStatus.WAITING_FOR_APPROVAL)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.error(SocketTimeoutException()))

    uiEvents.onNext(ScreenCreated())

    verify(userSession).refreshLoggedInUser()
  }
}
