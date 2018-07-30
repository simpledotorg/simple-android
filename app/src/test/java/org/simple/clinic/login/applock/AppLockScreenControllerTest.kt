package org.simple.clinic.login.applock

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.login.applock.PasswordHasher.ComparisonResult.DIFFERENT
import org.simple.clinic.login.applock.PasswordHasher.ComparisonResult.SAME
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant

class AppLockScreenControllerTest {

  private val screen = mock<AppLockScreen>()
  private val userSession = mock<UserSession>()
  private val passwordHasher = mock<PasswordHasher>()
  private val loggedInUser = PatientMocker.loggedInUser(pinDigest = "actual-hash")
  private val lastUnlockTimestamp = mock<Preference<Instant>>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  lateinit var controller: AppLockScreenController

  @Before
  fun setUp() {
    controller = AppLockScreenController(userSession, passwordHasher, lastUnlockTimestamp)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(loggedInUser)))

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when submit is clicked with a correct pin, the app should be unlocked`() {
    whenever(passwordHasher.compare(loggedInUser.pinDigest, "0000")).thenReturn(Single.just(SAME))

    uiEvents.onNext(AppLockScreenPinTextChanged("0000"))
    uiEvents.onNext(AppLockScreenSubmitClicked())

    val inOrder = inOrder(screen)
    inOrder.verify(screen).setProgressVisible(true)
    inOrder.verify(screen).restorePreviousScreen()
    verify(screen, never()).setProgressVisible(false)
  }

  @Test
  fun `when app is unlocked then the last-unlock-timestamp should be updated`() {
    whenever(passwordHasher.compare(any(), any())).thenReturn(Single.just(SAME))

    uiEvents.onNext(AppLockScreenPinTextChanged("0000"))
    uiEvents.onNext(AppLockScreenSubmitClicked())

    verify(lastUnlockTimestamp).delete()
  }

  @Test
  fun `when an incorrect pin is entered, an error should be shown`() {
    whenever(passwordHasher.compare(loggedInUser.pinDigest, "0000")).thenReturn(Single.just(DIFFERENT))

    uiEvents.onNext(AppLockScreenPinTextChanged("0000"))
    uiEvents.onNext(AppLockScreenSubmitClicked())

    val inOrder = inOrder(screen)
    inOrder.verify(screen).setProgressVisible(true)
    inOrder.verify(screen).setProgressVisible(false)
    inOrder.verify(screen).setIncorrectPinErrorVisible(true)
  }

  @Test
  fun `On start, the logged in user's full name should be shown`() {
    uiEvents.onNext(AppLockScreenCreated())
    verify(screen).showFullName(loggedInUser.fullName)
  }

  @Test
  fun `any existing errors should be reset when the user starts typing again`() {
    uiEvents.onNext(AppLockScreenPinTextChanged("0"))
    verify(screen).setIncorrectPinErrorVisible(false)
  }

  @Test
  fun `when logout is clicked, the ui action should be triggered`() {
    // Temporary test that will be changed later after the logout feature is implemented
    uiEvents.onNext(LogoutClicked())
    verify(screen).logoutDone()
  }

  @Test
  fun `when forgot pin is clicked, the ui action should be triggered`() {
    // Temporary test that will be changed later when the forgot PIN feature is implemented
    uiEvents.onNext(ForgotPinClicked())
    verify(screen).showCurrentPinResetRequestStatus()
  }
}
