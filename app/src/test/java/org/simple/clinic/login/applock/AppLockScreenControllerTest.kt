package org.simple.clinic.login.applock

import com.nhaarman.mockito_kotlin.mock
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

class AppLockScreenControllerTest {

  private val screen = mock<AppLockScreen>()
  private val userSession = mock<UserSession>()
  private val passwordHashser = mock<PasswordHasher>()
  private val loggedInUser = PatientMocker.loggedInUser(pinDigest = "actual-hash")

  private val uiEvents = PublishSubject.create<UiEvent>()
  lateinit var controller: AppLockScreenController

  @Before
  fun setUp() {
    controller = AppLockScreenController(userSession, passwordHashser)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(loggedInUser)))

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when submit is clicked with a correct pin, the app should be unlocked`() {
    whenever(passwordHashser.compare(loggedInUser.pinDigest, "0000")).thenReturn(Single.just(SAME))

    uiEvents.onNext(AppLockScreenPinTextChanged("0000"))
    uiEvents.onNext(AppLockScreenSubmitClicked())

    verify(screen).restorePreviousScreen()
  }

  @Test
  fun `when an incorrect pin is entered, an error should be shown`() {
    whenever(passwordHashser.compare(loggedInUser.pinDigest, "0000")).thenReturn(Single.just(DIFFERENT))

    uiEvents.onNext(AppLockScreenPinTextChanged("0000"))
    uiEvents.onNext(AppLockScreenSubmitClicked())

    verify(screen).showIncorrectPinError()
  }

  @Test
  fun `On start, the logged in user's phone-number should be shown`() {
    uiEvents.onNext(AppLockScreenCreated())
    verify(screen).showPhoneNumber(loggedInUser.phoneNumber)
  }

  @Test
  fun `any existing errors should be reset when the user starts typing again`() {
    uiEvents.onNext(AppLockScreenPinTextChanged("0"))
    verify(screen).hideIncorrectPinError()
  }
}
