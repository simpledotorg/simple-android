package org.simple.clinic.activity

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.empty.EmptyScreenKey
import org.simple.clinic.forgotpin.createnewpin.ForgotPinCreateNewPinScreen
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.login.applock.AppLockScreenKey
import org.simple.clinic.main.TheActivityEffect
import org.simple.clinic.main.TheActivityEffectHandler
import org.simple.clinic.main.TheActivityEvent
import org.simple.clinic.main.TheActivityInit
import org.simple.clinic.main.TheActivityModel
import org.simple.clinic.main.TheActivityUi
import org.simple.clinic.main.TheActivityUiRenderer
import org.simple.clinic.main.TheActivityUpdate
import org.simple.clinic.navigation.v2.History
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.storage.MemoryValue
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.sharedTestCode.util.RxErrorsRule
import org.simple.sharedTestCode.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.Duration
import java.time.Instant
import java.util.Optional
import java.util.UUID
import java.util.concurrent.TimeUnit

class TheActivityControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val lockInMinutes = 15L

  private val ui = mock<TheActivityUi>()
  private val userSession = mock<UserSession>()
  private val patientRepository = mock<PatientRepository>()
  private val uiEvents = PublishSubject.create<UiEvent>()

  private val currentTimestamp = Instant.parse("2018-01-01T00:00:00Z")
  private val clock = TestUtcClock(currentTimestamp)
  private val user = TestData.loggedInUser(
      uuid = UUID.fromString("049ee3e0-f5a8-4ba6-9270-b20231d3fe50"),
      loggedInStatus = LOGGED_IN,
      status = UserStatus.ApprovedForSyncing
  )

  private lateinit var testFixture: MobiusTestFixture<TheActivityModel, TheActivityEvent, TheActivityEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when activity is started, user has requested an OTP, and user was inactive then app lock should be shown`() {
    // given

    whenever(userSession.loggedInUserImmediate()).thenReturn(user.otpRequested())
    val lockAfterTime = currentTimestamp.minusSeconds(TimeUnit.MINUTES.toSeconds(1))

    // when
    setupController(lockAtTime = lockAfterTime)

    // then
    verify(ui).setCurrentScreenHistory(History.ofNormalScreens(AppLockScreenKey(History.ofNormalScreens(HomeScreenKey))))
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when activity is started, user is logged in, and user was inactive then app lock should be shown`() {
    // given
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)

    val lockAfterTime = currentTimestamp.minusSeconds(TimeUnit.MINUTES.toSeconds(1))

    // when
    setupController(lockAtTime = lockAfterTime)

    // then
    verify(ui).setCurrentScreenHistory(History.ofNormalScreens(AppLockScreenKey(History.ofNormalScreens(HomeScreenKey))))
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when activity is started, user has requested a PIN reset, and user was inactive then app lock should be shown`() {
    // given
    whenever(userSession.loggedInUserImmediate()).thenReturn(user.resetPinRequested())
    val lockAfterTime = currentTimestamp.minusSeconds(TimeUnit.MINUTES.toSeconds(1))

    // when
    setupController(lockAtTime = lockAfterTime)

    // then
    verify(ui).setCurrentScreenHistory(History.ofNormalScreens(AppLockScreenKey(History.ofNormalScreens(HomeScreenKey))))
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when activity is started, user is resetting the PIN, and user was inactive then app lock should not be shown`() {
    // given
    whenever(userSession.loggedInUserImmediate()).thenReturn(user.resettingPin())

    val lockAfterTime = currentTimestamp.minusSeconds(TimeUnit.MINUTES.toSeconds(1))

    // when
    setupController(lockAtTime = lockAfterTime)

    // then
    verify(ui).setCurrentScreenHistory(History.ofNormalScreens(ForgotPinCreateNewPinScreen.Key()))
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when app is started unlocked and lock timer hasn't expired yet then the timer should be unset`() {
    // given
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)

    val lockAfterTimestamp = MemoryValue(
        defaultValue = Optional.empty(),
        currentValue = Optional.of(Instant.now().plusSeconds(TimeUnit.MINUTES.toSeconds(10)))
    )

    // when
    setupController(lockAfterTimestamp = lockAfterTimestamp)

    // then
    assertThat(lockAfterTimestamp.hasValue).isFalse()
    verify(ui).setCurrentScreenHistory(History.ofNormalScreens(HomeScreenKey))
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when app is started locked and lock timer hasn't expired yet then the timer should not be unset`() {
    // given
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)
    val lockAfterTimestamp = MemoryValue(
        defaultValue = Optional.empty(),
        currentValue = Optional.of(currentTimestamp.minusSeconds(TimeUnit.MINUTES.toSeconds(5)))
    )

    // when
    setupController(lockAfterTimestamp = lockAfterTimestamp)

    // then
    assertThat(lockAfterTimestamp.hasValue).isTrue()
    verify(ui).setCurrentScreenHistory(History.ofNormalScreens(AppLockScreenKey(History.ofNormalScreens(HomeScreenKey))))
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `the logged out alert must be shown only at the instant when a user gets verified for login`() {
    // given
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)
    val userStream: Observable<Optional<User>> = Observable.just(
        Optional.of(user.otpRequested()),
        Optional.of(user),
        Optional.of(user)
    )

    // when
    setupController(
        userStream = userStream,
        lockAtTime = currentTimestamp.plus(Duration.ofMinutes(lockInMinutes))
    )

    // then
    verify(ui).setCurrentScreenHistory(History.ofNormalScreens(HomeScreenKey))
    verify(ui).showUserLoggedOutOnOtherDeviceAlert()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `the logged out alert must not be shown if the user is already logged in when the screen is opened`() {
    // given
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)
    whenever(userSession.loggedInUser()).thenReturn(
        Observable.just(
            Optional.of(user),
            Optional.of(user),
            Optional.of(user))
    )

    // when
    setupController()

    // then
    verify(ui).setCurrentScreenHistory(History.ofNormalScreens(AppLockScreenKey(History.ofNormalScreens(HomeScreenKey))))
    verify(ui, never()).showUserLoggedOutOnOtherDeviceAlert()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when user is denied access then access denied screen should show`() {
    //given
    val fullName = "Anish Acharya"
    val loggedInUser = user
        .withFullName(fullName)
    whenever(userSession.loggedInUserImmediate()).thenReturn(loggedInUser)
    whenever(patientRepository.clearPatientData()).thenReturn(Completable.complete())
    val userDisapprovedSubject = PublishSubject.create<Boolean>()

    //when
    setupController(
        userDisapprovedStream = userDisapprovedSubject,
        lockAtTime = Instant.now(clock)
    )
    // then
    verify(ui).setCurrentScreenHistory(History.ofNormalScreens(HomeScreenKey))
    verifyNoMoreInteractions(ui)
    verify(patientRepository, never()).clearPatientData()

    // when
    userDisapprovedSubject.onNext(true)

    // then
    verify(patientRepository).clearPatientData()
    verify(ui).showAccessDeniedScreen(fullName)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when user has access then the access denied screen should not appear`() {
    //given
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(user.toOptional()))

    //when
    setupController(lockAtTime = Instant.now(clock))

    //then
    verify(ui).setCurrentScreenHistory(History.ofNormalScreens(HomeScreenKey))
    verifyNoMoreInteractions(ui)
    verify(patientRepository, never()).clearPatientData()
  }

  @Test
  fun `the sign in screen must be shown only at the moment where the user gets logged out`() {
    // given
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)
    val userUnauthorizedSubject = PublishSubject.create<Boolean>()

    // when
    setupController(userUnauthorizedStream = userUnauthorizedSubject)
    userUnauthorizedSubject.onNext(false)

    // then
    verify(ui, never()).redirectToLogin()

    // when
    userUnauthorizedSubject.onNext(true)

    // then
    verify(ui).redirectToLogin()

    clearInvocations(ui)

    // when
    userUnauthorizedSubject.onNext(true)

    // then
    verifyZeroInteractions(ui)

    // when
    userUnauthorizedSubject.onNext(false)

    // then
    verifyZeroInteractions(ui)

    // when
    userUnauthorizedSubject.onNext(true)

    // then
    verify(ui).redirectToLogin()

    verifyNoMoreInteractions(ui)
  }

  private fun setupController(
      userStream: Observable<Optional<User>> = Observable.just(Optional.empty()),
      userUnauthorizedStream: Observable<Boolean> = Observable.just(false),
      userDisapprovedStream: Observable<Boolean> = Observable.just(false),
      lockAtTime: Instant? = null
  ) {
    setupController(
        userStream = userStream,
        userUnauthorizedStream = userUnauthorizedStream,
        userDisapprovedStream = userDisapprovedStream,
        lockAfterTimestamp = MemoryValue(Optional.ofNullable(lockAtTime))
    )
  }

  private fun setupController(
      userStream: Observable<Optional<User>> = Observable.just(Optional.of(user)),
      userUnauthorizedStream: Observable<Boolean> = Observable.just(false),
      userDisapprovedStream: Observable<Boolean> = Observable.just(false),
      lockAfterTimestamp: MemoryValue<Optional<Instant>>
  ) {
    whenever(userSession.isUserUnauthorized()).thenReturn(userUnauthorizedStream)
    whenever(userSession.loggedInUser()).thenReturn(userStream)
    whenever(userSession.isUserDisapproved()).thenReturn(userDisapprovedStream)

    val currentHistory = History.ofNormalScreens(EmptyScreenKey().wrap())
    val effectHandler = TheActivityEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        userSession = userSession,
        utcClock = clock,
        patientRepository = patientRepository,
        lockAfterTimestamp = lockAfterTimestamp,
        uiActions = ui,
        provideCurrentScreenHistory = { currentHistory }
    )
    val uiRenderer = TheActivityUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = TheActivityModel.createForAlreadyLoggedInUser(),
        update = TheActivityUpdate(),
        effectHandler = effectHandler.build(),
        init = TheActivityInit(),
        modelUpdateListener = uiRenderer::render
    )
    testFixture.start()
  }

  private fun User.otpRequested(): User = copy(loggedInStatus = OTP_REQUESTED)

  private fun User.resetPinRequested(): User = copy(loggedInStatus = RESET_PIN_REQUESTED)

  private fun User.resettingPin(): User = copy(loggedInStatus = RESETTING_PIN)

  private fun User.disapprovedForSyncing(): User = copy(status = UserStatus.DisapprovedForSyncing)
}
