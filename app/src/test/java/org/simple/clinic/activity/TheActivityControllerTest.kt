package org.simple.clinic.activity

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.activity.ActivityLifecycle.Started
import org.simple.clinic.activity.ActivityLifecycle.Stopped
import org.simple.clinic.login.applock.AppLockConfig
import org.simple.clinic.main.TheActivity
import org.simple.clinic.main.TheActivityController
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.NOT_LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.TimeUnit

class TheActivityControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val lockInMinutes = 15L

  private val activity = mock<TheActivity>()
  private val userSession = mock<UserSession>()
  private val patientRepository = mock<PatientRepository>()
  private val lockAfterTimestamp = mock<Preference<Instant>>()
  private val uiEvents = PublishSubject.create<UiEvent>()

  private val currentTimestamp = Instant.parse("2018-01-01T00:00:00Z")
  private val clock = TestUtcClock(currentTimestamp)

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when activity is started and user is logged out then app lock shouldn't be shown`() {
    // given
    whenever(userSession.isUserLoggedIn()).thenReturn(false)

    // when
    setupController()

    // then
    verify(activity, never()).showAppLockScreen()
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun `when activity is started, user has requested an OTP, and user was inactive then app lock should be shown`() {
    // given
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    val userStream: Observable<Optional<User>> = Observable.just(Optional.of(TestData.loggedInUser(
        uuid = UUID.fromString("049ee3e0-f5a8-4ba6-9270-b20231d3fe50"),
        loggedInStatus = OTP_REQUESTED,
        status = UserStatus.ApprovedForSyncing
    )))

    val lockAfterTime = currentTimestamp.minusSeconds(TimeUnit.MINUTES.toSeconds(1))
    whenever(lockAfterTimestamp.get()).thenReturn(lockAfterTime)

    // when
    setupController(userStream = userStream)

    // then
    verify(activity).showAppLockScreen()
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun `when activity is started, user is logged in, and user was inactive then app lock should be shown`() {
    // given
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    val userStream: Observable<Optional<User>> = Observable.just(Optional.of(TestData.loggedInUser(
        uuid = UUID.fromString("049ee3e0-f5a8-4ba6-9270-b20231d3fe50"),
        loggedInStatus = LOGGED_IN,
        status = UserStatus.ApprovedForSyncing
    )))

    val lockAfterTime = currentTimestamp.minusSeconds(TimeUnit.MINUTES.toSeconds(1))
    whenever(lockAfterTimestamp.get()).thenReturn(lockAfterTime)

    // when
    setupController(userStream = userStream)

    // then
    verify(activity).showAppLockScreen()
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun `when activity is started, user has requested a PIN reset, and user was inactive then app lock should be shown`() {
    // given
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    val userStream: Observable<Optional<User>> = Observable.just(Optional.of(TestData.loggedInUser(
        uuid = UUID.fromString("049ee3e0-f5a8-4ba6-9270-b20231d3fe50"),
        loggedInStatus = RESET_PIN_REQUESTED,
        status = UserStatus.ApprovedForSyncing
    )))

    val lockAfterTime = currentTimestamp.minusSeconds(TimeUnit.MINUTES.toSeconds(1))
    whenever(lockAfterTimestamp.get()).thenReturn(lockAfterTime)

    // when
    setupController(userStream = userStream)

    // then
    verify(activity).showAppLockScreen()
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun `when activity is started, user is not logged in and user was inactive then app lock should not be shown`() {
    // given
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    whenever(userSession.loggedInUser())
        .thenReturn(Observable.just(Optional.of(TestData.loggedInUser(
            uuid = UUID.fromString("049ee3e0-f5a8-4ba6-9270-b20231d3fe50"),
            loggedInStatus = NOT_LOGGED_IN,
            status = UserStatus.ApprovedForSyncing
        ))))

    val lockAfterTime = currentTimestamp.minusSeconds(TimeUnit.MINUTES.toSeconds(1))
    whenever(lockAfterTimestamp.get()).thenReturn(lockAfterTime)

    //when
    setupController()

    // then
    verify(activity, never()).showAppLockScreen()
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun `when activity is started, user is resetting the PIN, and user was inactive then app lock should not be shown`() {
    // given
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    whenever(userSession.loggedInUser())
        .thenReturn(Observable.just(Optional.of(TestData.loggedInUser(
            uuid = UUID.fromString("049ee3e0-f5a8-4ba6-9270-b20231d3fe50"),
            loggedInStatus = RESETTING_PIN,
            status = UserStatus.ApprovedForSyncing
        ))))

    val lockAfterTime = currentTimestamp.minusSeconds(TimeUnit.MINUTES.toSeconds(1))
    whenever(lockAfterTimestamp.get()).thenReturn(lockAfterTime)

    // when
    setupController()

    // then
    verify(activity, never()).showAppLockScreen()
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun `when app is stopped and lock timer is unset then the timer should be updated`() {
    // given
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    whenever(lockAfterTimestamp.get()).thenReturn(Instant.MAX)
    whenever(lockAfterTimestamp.isSet).thenReturn(false)

    // when
    setupController()
    uiEvents.onNext(Stopped(null))

    // then
    verify(lockAfterTimestamp).set(currentTimestamp.plus(lockInMinutes, ChronoUnit.MINUTES))
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun `when app is stopped and lock timer is set then the timer should not be updated`() {
    // given
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    whenever(lockAfterTimestamp.isSet).thenReturn(true)
    whenever(lockAfterTimestamp.get()).thenReturn(Instant.now())

    // when
    setupController()
    uiEvents.onNext(Stopped(null))

    // then
    verify(lockAfterTimestamp, never()).set(any())
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun `when app is started unlocked and lock timer hasn't expired yet then the timer should be unset`() {
    // given
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    val userStream: Observable<Optional<User>> = Observable.just(Optional.of(TestData.loggedInUser(
        uuid = UUID.fromString("049ee3e0-f5a8-4ba6-9270-b20231d3fe50"),
        loggedInStatus = LOGGED_IN,
        status = UserStatus.ApprovedForSyncing
    )))

    val lockAfterTime = Instant.now().plusSeconds(TimeUnit.MINUTES.toSeconds(10))
    whenever(lockAfterTimestamp.get()).thenReturn(lockAfterTime)

    // when
    setupController(userStream = userStream)

    // then
    verify(lockAfterTimestamp).delete()
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun `when app is started locked and lock timer hasn't expired yet then the timer should not be unset`() {
    // given
    whenever(userSession.isUserLoggedIn()).thenReturn(true)

    val lockAfterTime = Instant.now().minusSeconds(TimeUnit.MINUTES.toSeconds(5))
    whenever(lockAfterTimestamp.get()).thenReturn(lockAfterTime)

    // when
    setupController()

    // then
    verify(lockAfterTimestamp, never()).delete()
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun `the logged out alert must be shown only at the instant when a user gets verified for login`() {
    // given
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("bed4a670-7f03-44ab-87ca-f297ca35375a"),
        status = UserStatus.ApprovedForSyncing,
        loggedInStatus = OTP_REQUESTED
    )
    whenever(lockAfterTimestamp.get()).thenReturn(Instant.MAX)

    val userStream: Observable<Optional<User>> = Observable.just(
        Optional.of(user),
        Optional.of(user.copy(loggedInStatus = LOGGED_IN)),
        Optional.of(user.copy(loggedInStatus = LOGGED_IN))
    )

    // when
    setupController(userStream = userStream)

    // then
    verify(activity).showUserLoggedOutOnOtherDeviceAlert()
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun `the logged out alert must not be shown if the user is already logged in when the screen is opened`() {
    // given
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("bed4a670-7f03-44ab-87ca-f297ca35375a"),
        status = UserStatus.ApprovedForSyncing,
        loggedInStatus = LOGGED_IN
    )
    whenever(lockAfterTimestamp.get()).thenReturn(Instant.MAX)
    whenever(userSession.loggedInUser()).thenReturn(
        Observable.just(
            Optional.of(user),
            Optional.of(user.copy(loggedInStatus = LOGGED_IN)),
            Optional.of(user.copy(loggedInStatus = LOGGED_IN)))
    )

    // when
    setupController()

    // then
    verify(activity, never()).showUserLoggedOutOnOtherDeviceAlert()
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun `when user is denied access then access denied screen should show`() {
    //given
    val fullName = "Anish Acharya"
    val loggedInUser = TestData.loggedInUser(
        uuid = UUID.fromString("0b350f89-ed0e-4922-b384-7f7a9bf3aba0"),
        name = fullName,
        status = UserStatus.DisapprovedForSyncing,
        loggedInStatus = LOGGED_IN
    )
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(loggedInUser.toOptional()))
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    whenever(lockAfterTimestamp.get()).thenReturn(Instant.now())
    whenever(patientRepository.clearPatientData()).thenReturn(Completable.complete())
    whenever(userSession.loggedInUserImmediate()).thenReturn(loggedInUser)
    val userDisapprovedSubject = PublishSubject.create<Boolean>()

    //when
    setupController(userDisapprovedStream = userDisapprovedSubject)
    userDisapprovedSubject.onNext(true)

    //then
    verify(patientRepository).clearPatientData()
    verify(activity).showAccessDeniedScreen(fullName)
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun `when user has access then the access denied screen should not appear`() {
    //given
    val fullName = "Anish Acharya"
    val loggedInUser = TestData.loggedInUser(
        uuid = UUID.fromString("0b350f89-ed0e-4922-b384-7f7a9bf3aba0"),
        name = fullName,
        status = UserStatus.ApprovedForSyncing,
        loggedInStatus = LOGGED_IN
    )
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(loggedInUser.toOptional()))
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    whenever(lockAfterTimestamp.get()).thenReturn(Instant.now())

    //when
    setupController()

    //then
    verify(activity, never()).showAccessDeniedScreen(fullName)
    verifyNoMoreInteractions(activity)
    verify(patientRepository, never()).clearPatientData()
  }

  @Test
  fun `the sign in screen must be shown only at the moment where the user gets logged out`() {
    // given
    val userUnauthorizedSubject = PublishSubject.create<Boolean>()

    // when
    setupController(userUnauthorizedStream = userUnauthorizedSubject)
    userUnauthorizedSubject.onNext(false)

    // then
    verify(activity, never()).redirectToLogin()

    // when
    userUnauthorizedSubject.onNext(true)

    // then
    verify(activity).redirectToLogin()

    clearInvocations(activity)

    // when
    userUnauthorizedSubject.onNext(true)
    
    // then
    verifyZeroInteractions(activity)

    // when
    userUnauthorizedSubject.onNext(false)

    // then
    verifyZeroInteractions(activity)

    // when
    userUnauthorizedSubject.onNext(true)

    // then
    verify(activity).redirectToLogin()

    verifyNoMoreInteractions(activity)
  }

  private fun setupController(
      userStream: Observable<Optional<User>> = Observable.just(Optional.empty()),
      userUnauthorizedStream: Observable<Boolean> = Observable.just(false),
      userDisapprovedStream: Observable<Boolean> = Observable.just(false)
  ) {
    whenever(userSession.isUserUnauthorized()).thenReturn(userUnauthorizedStream)
    whenever(userSession.loggedInUser()).thenReturn(userStream)
    whenever(userSession.isUserDisapproved()).thenReturn(userDisapprovedStream)

    val controller = TheActivityController(
        userSession = userSession,
        appLockConfig = AppLockConfig(lockAfterTimeMillis = TimeUnit.MINUTES.toMillis(lockInMinutes)),
        patientRepository = patientRepository,
        utcClock = clock,
        lockAfterTimestamp = lockAfterTimestamp
    )

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(activity) }

    uiEvents.onNext(Started(null))
  }
}
