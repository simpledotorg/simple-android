package org.simple.clinic.activity

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
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
import org.simple.clinic.forgotpin.createnewpin.ForgotPinCreateNewPinScreenKey
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.login.applock.AppLockConfig
import org.simple.clinic.onboarding.OnboardingScreenKey
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.registration.phone.RegistrationPhoneScreenKey
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.NOT_LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.UNAUTHORIZED
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit

@RunWith(JUnitParamsRunner::class)
class TheActivityControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val lockInMinutes = 15L

  private val activity = mock<TheActivity>()
  private val userSession = mock<UserSession>()
  private val lockAfterTimestamp = mock<Preference<Instant>>()
  private val hasUserCompletedOnboarding = mock<Preference<Boolean>>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val userSubject = PublishSubject.create<Optional<User>>()
  private val userUnauthorizedSubject = PublishSubject.create<Boolean>()

  private val controller = TheActivityController(
      userSession = userSession,
      appLockConfig = Single.just(AppLockConfig(lockAfterTimeMillis = TimeUnit.MINUTES.toMillis(lockInMinutes))),
      lockAfterTimestamp = lockAfterTimestamp,
      hasUserCompletedOnboarding = hasUserCompletedOnboarding
  )

  @Before
  fun setUp() {
    whenever(userSession.isUserUnauthorized()).thenReturn(userUnauthorizedSubject)
    whenever(userSession.loggedInUser()).thenReturn(userSubject)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(activity) }
  }

  @Test
  fun `when activity is started and user is logged out then app lock shouldn't be shown`() {
    whenever(userSession.isUserLoggedIn()).thenReturn(false)

    uiEvents.onNext(TheActivityLifecycle.Started())

    verify(activity, never()).showAppLockScreen()
  }

  @Test
  @Parameters(value = [
    "NOT_LOGGED_IN|false",
    "OTP_REQUESTED|true",
    "LOGGED_IN|true",
    "RESETTING_PIN|false",
    "RESET_PIN_REQUESTED|true"
  ])
  fun `when activity is started, user is logged in and user was inactive then app lock should be shown`(
      loggedInStatus: User.LoggedInStatus,
      shouldShowAppLock: Boolean
  ) {
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(PatientMocker.loggedInUser(loggedInStatus = loggedInStatus))))

    val lockAfterTime = Instant.now().minusSeconds(TimeUnit.MINUTES.toSeconds(1))
    whenever(lockAfterTimestamp.get()).thenReturn(lockAfterTime)

    uiEvents.onNext(TheActivityLifecycle.Started())

    if (shouldShowAppLock) {
      verify(activity).showAppLockScreen()
    } else {
      verify(activity, never()).showAppLockScreen()
    }
  }

  @Test
  fun `when app is stopped and lock timer is unset then the timer should be updated`() {
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    whenever(lockAfterTimestamp.get()).thenReturn(Instant.MAX)
    whenever(lockAfterTimestamp.isSet).thenReturn(false)

    uiEvents.onNext(TheActivityLifecycle.Stopped())

    verify(lockAfterTimestamp).set(check {
      // Not the best way, but works.
      it > Instant.now().minusSeconds(1)
    })
  }

  @Test
  fun `when app is stopped and lock timer is set then the timer should not be updated`() {
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    whenever(lockAfterTimestamp.isSet).thenReturn(true)
    whenever(lockAfterTimestamp.get()).thenReturn(Instant.now())

    uiEvents.onNext(TheActivityLifecycle.Stopped())

    verify(lockAfterTimestamp, never()).set(any())
  }

  @Test
  fun `when app is started unlocked and lock timer hasn't expired yet then the timer should be unset`() {
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    whenever(userSession.loggedInUser())
        .thenReturn(Observable.just(Just(PatientMocker.loggedInUser(loggedInStatus = LOGGED_IN))))

    val lockAfterTime = Instant.now().plusSeconds(TimeUnit.MINUTES.toSeconds(10))
    whenever(lockAfterTimestamp.get()).thenReturn(lockAfterTime)

    uiEvents.onNext(TheActivityLifecycle.Started())

    verify(lockAfterTimestamp).delete()
  }

  @Test
  fun `when app is started locked and lock timer hasn't expired yet then the timer should not be unset`() {
    whenever(userSession.isUserLoggedIn()).thenReturn(true)

    val lockAfterTime = Instant.now().minusSeconds(TimeUnit.MINUTES.toSeconds(5))
    whenever(lockAfterTimestamp.get()).thenReturn(lockAfterTime)

    uiEvents.onNext(TheActivityLifecycle.Started())

    verify(lockAfterTimestamp, never()).delete()
  }

  @Test
  @Parameters(
      "OTP_REQUESTED|LOGGED_IN|LOGGED_IN|true",
      "LOGGED_IN|LOGGED_IN|LOGGED_IN|false"
  )
  fun `when a user is verified for login, the logged out alert must be shown`(
      prevloggedInStatus: User.LoggedInStatus,
      curLoggedInStatus: User.LoggedInStatus,
      nextLoggedInStatus: User.LoggedInStatus,
      shouldShowLoggedOutAlert: Boolean
  ) {
    val user = PatientMocker.loggedInUser(status = UserStatus.ApprovedForSyncing, loggedInStatus = prevloggedInStatus)
    whenever(lockAfterTimestamp.get()).thenReturn(Instant.MAX)
    whenever(userSession.loggedInUser()).thenReturn(
        Observable.just(
            Just(user),
            Just(user.copy(loggedInStatus = curLoggedInStatus)),
            Just(user.copy(loggedInStatus = nextLoggedInStatus)))
    )

    uiEvents.onNext(TheActivityLifecycle.Started())

    if (shouldShowLoggedOutAlert) {
      verify(activity).showUserLoggedOutOnOtherDeviceAlert()
    } else {
      verify(activity, never()).showUserLoggedOutOnOtherDeviceAlert()
    }
  }

  @Test
  @Parameters(method = "params for local user initial screen key")
  fun `when a local user exists, the appropriate initial key must be returned based on the logged in status`(
      hasCompletedOnboarding: Boolean,
      loggedInStatus: User.LoggedInStatus,
      expectedKeyType: Class<FullScreenKey>
  ) {
    val user = PatientMocker.loggedInUser(loggedInStatus = loggedInStatus)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))

    whenever(hasUserCompletedOnboarding.get()).thenReturn(hasCompletedOnboarding)

    assertThat(controller.initialScreenKey()).isInstanceOf(expectedKeyType)
  }

  @Suppress("Unused")
  private fun `params for local user initial screen key`(): Array<Array<Any>> {
    fun testCase(
        hasCompletedOnboarding: Boolean,
        loggedInStatus: User.LoggedInStatus,
        expectedKeyType: Class<*>
    ): Array<Any> {
      return arrayOf(hasCompletedOnboarding, loggedInStatus, expectedKeyType)
    }

    return arrayOf(
        testCase(
            hasCompletedOnboarding = true,
            loggedInStatus = NOT_LOGGED_IN,
            expectedKeyType = RegistrationPhoneScreenKey::class.java
        ),
        testCase(
            hasCompletedOnboarding = true,
            loggedInStatus = OTP_REQUESTED,
            expectedKeyType = HomeScreenKey::class.java
        ),
        testCase(
            hasCompletedOnboarding = true,
            loggedInStatus = RESET_PIN_REQUESTED,
            expectedKeyType = HomeScreenKey::class.java
        ),
        testCase(
            hasCompletedOnboarding = true,
            loggedInStatus = RESETTING_PIN,
            expectedKeyType = ForgotPinCreateNewPinScreenKey::class.java
        ),
        testCase(
            hasCompletedOnboarding = true,
            loggedInStatus = LOGGED_IN,
            expectedKeyType = HomeScreenKey::class.java
        ),
        testCase(
            hasCompletedOnboarding = true,
            loggedInStatus = UNAUTHORIZED,
            expectedKeyType = HomeScreenKey::class.java
        ),
        testCase(
            hasCompletedOnboarding = false,
            loggedInStatus = NOT_LOGGED_IN,
            expectedKeyType = OnboardingScreenKey::class.java
        ),
        testCase(
            hasCompletedOnboarding = false,
            loggedInStatus = OTP_REQUESTED,
            expectedKeyType = HomeScreenKey::class.java
        ),
        testCase(
            hasCompletedOnboarding = false,
            loggedInStatus = RESET_PIN_REQUESTED,
            expectedKeyType = HomeScreenKey::class.java
        ),
        testCase(
            hasCompletedOnboarding = false,
            loggedInStatus = RESETTING_PIN,
            expectedKeyType = OnboardingScreenKey::class.java
        ),
        testCase(
            hasCompletedOnboarding = false,
            loggedInStatus = LOGGED_IN,
            expectedKeyType = HomeScreenKey::class.java
        ),
        testCase(
            hasCompletedOnboarding = false,
            loggedInStatus = UNAUTHORIZED,
            expectedKeyType = HomeScreenKey::class.java
        )
    )
  }

  @Test
  @Parameters(method = "params for missing user initial screen key")
  fun `when a local user does not exist, the appropriate initial key must be returned based on the onboarding flag`(
      hasCompletedOnboarding: Boolean,
      expectedKeyType: Class<FullScreenKey>
  ) {
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(None))
    whenever(hasUserCompletedOnboarding.get()).thenReturn(hasCompletedOnboarding)

    assertThat(controller.initialScreenKey()).isInstanceOf(expectedKeyType)
  }

  @Suppress("Unused")
  private fun `params for missing user initial screen key`(): Array<Array<Any>> {
    return arrayOf(
        arrayOf<Any>(true, RegistrationPhoneScreenKey()::class.java),
        arrayOf<Any>(false, OnboardingScreenKey()::class.java)
    )
  }

  data class RedirectToSignInParams(
      val userUnauthorizedValues: List<Boolean>,
      val numberOfTimesShouldRedirectToSignIn: Int
  )

  @Test
  @Parameters(method = "params for redirecting to sign in")
  fun `whenever the user logged in status becomes unauthorized, the sign in screen must be shown`(testCase: RedirectToSignInParams) {
    val (userUnauthorizedValues, numberOfTimesShouldRedirectToSignIn) = testCase
    userUnauthorizedValues.forEach(userUnauthorizedSubject::onNext)

    if (numberOfTimesShouldRedirectToSignIn > 0) {
      verify(activity, times(numberOfTimesShouldRedirectToSignIn)).redirectToLogin()
    } else {
      verify(activity, never()).redirectToLogin()
    }
    verifyNoMoreInteractions(activity)
  }

  @Suppress("Unused")
  private fun `params for redirecting to sign in`(): List<RedirectToSignInParams> {
    return listOf(
        RedirectToSignInParams(
            userUnauthorizedValues = listOf(true),
            numberOfTimesShouldRedirectToSignIn = 1
        ),
        RedirectToSignInParams(
            userUnauthorizedValues = listOf(false),
            numberOfTimesShouldRedirectToSignIn = 0
        ),
        RedirectToSignInParams(
            userUnauthorizedValues = listOf(true, true),
            numberOfTimesShouldRedirectToSignIn = 1
        ),
        RedirectToSignInParams(
            userUnauthorizedValues = listOf(true, false, true),
            numberOfTimesShouldRedirectToSignIn = 2
        ),
        RedirectToSignInParams(
            userUnauthorizedValues = listOf(false, true, true, false, true),
            numberOfTimesShouldRedirectToSignIn = 2
        ),
        RedirectToSignInParams(
            userUnauthorizedValues = listOf(false, false, false, false),
            numberOfTimesShouldRedirectToSignIn = 0
        ),
        RedirectToSignInParams(
            userUnauthorizedValues = listOf(true, true, true, true),
            numberOfTimesShouldRedirectToSignIn = 1
        ),
        RedirectToSignInParams(
            userUnauthorizedValues = listOf(true, false, true, false, true, false),
            numberOfTimesShouldRedirectToSignIn = 3
        )
    )
  }
}
