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
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.login.applock.ComparisonResult.DIFFERENT
import org.simple.clinic.login.applock.ComparisonResult.SAME
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant

class AppLockScreenControllerTest {

  private val screen = mock<AppLockScreen>()
  private val userSession = mock<UserSession>()
  private val passwordHashser = mock<PasswordHasher>()
  private val facilityRepository = mock<FacilityRepository>()
  private val lastUnlockTimestamp = mock<Preference<Instant>>()

  private val loggedInUser = PatientMocker.loggedInUser(pinDigest = "actual-hash")

  private val uiEvents = PublishSubject.create<UiEvent>()
  lateinit var controller: AppLockScreenController

  @Before
  fun setUp() {
    controller = AppLockScreenController(userSession, passwordHashser, facilityRepository, lastUnlockTimestamp)
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

    val inOrder = inOrder(screen)
    inOrder.verify(screen).setProgressVisible(true)
    inOrder.verify(screen).restorePreviousScreen()
    verify(screen, never()).setProgressVisible(false)
  }

  @Test
  fun `when app is unlocked then the last-unlock-timestamp should be updated`() {
    whenever(passwordHashser.compare(any(), any())).thenReturn(Single.just(SAME))

    uiEvents.onNext(AppLockScreenPinTextChanged("0000"))
    uiEvents.onNext(AppLockScreenSubmitClicked())

    verify(lastUnlockTimestamp).delete()
  }

  @Test
  fun `when an incorrect pin is entered, an error should be shown`() {
    whenever(passwordHashser.compare(loggedInUser.pinDigest, "0000")).thenReturn(Single.just(DIFFERENT))

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
    verify(screen).setUserFullName(loggedInUser.fullName)
  }

  @Test
  fun `On start, the currently selected facility should be shown`() {
    val facility1 = PatientMocker.facility(name = "facility1")
    val facility2 = PatientMocker.facility(name = "facility2")
    whenever(facilityRepository.currentFacility(userSession)).thenReturn(Observable.just(facility1, facility2))

    uiEvents.onNext(AppLockScreenCreated())
    verify(screen).setFacilityName(facility1.name)
    verify(screen).setFacilityName(facility2.name)
  }

  @Test
  fun `any existing errors should be reset when the user starts typing again`() {
    uiEvents.onNext(AppLockScreenPinTextChanged("0"))
    verify(screen).setIncorrectPinErrorVisible(false)
  }

  @Test
  fun `when facility name is clicked then facility change screen should be shown`() {
    uiEvents.onNext(AppLockFacilityClicked())
    verify(screen).openFacilityChangeScreen()
  }

  @Test
  fun `when forgot pin is clicked then the confirm forgot pin alert must be shown`() {
    uiEvents.onNext(AppLockForgotPinClicked())
    verify(screen).showConfirmResetPinDialog()
  }
}
