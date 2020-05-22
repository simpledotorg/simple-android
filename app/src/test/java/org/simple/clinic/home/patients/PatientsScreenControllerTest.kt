package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.activity.ActivityLifecycle.Resumed
import org.simple.clinic.appupdate.AppUpdateState
import org.simple.clinic.appupdate.AppUpdateState.AppUpdateStateError
import org.simple.clinic.appupdate.AppUpdateState.DontShowAppUpdate
import org.simple.clinic.appupdate.AppUpdateState.ShowAppUpdate
import org.simple.clinic.appupdate.CheckAppUpdateAvailability
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus.ApprovedForSyncing
import org.simple.clinic.user.UserStatus.WaitingForApproval
import org.simple.clinic.user.refreshuser.RefreshCurrentUser
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.platform.util.RuntimePermissionResult.DENIED
import org.simple.clinic.platform.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.net.SocketTimeoutException
import java.util.UUID

class PatientsScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui: PatientsUi = mock()
  private val userSession = mock<UserSession>()
  private val approvalStatusApprovedAtPreference = mock<Preference<Instant>>()
  private val hasUserDismissedApprovedStatusPreference = mock<Preference<Boolean>>()
  private val checkAppUpdate = mock<CheckAppUpdateAvailability>()
  private val appUpdateDialogShownPref = mock<Preference<Instant>>()

  private val date = LocalDate.parse("2018-01-01")
  private val dateAsInstant = Instant.parse("2018-01-01T00:00:00Z")
  private val utcClock = TestUtcClock(date)
  private val userClock = TestUserClock(date)

  private val numberOfPatientsRegisteredPreference = mock<Preference<Int>>()
  private val refreshCurrentUser = mock<RefreshCurrentUser>()

  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()

  private val userUuid = UUID.fromString("df863f94-a6f9-4409-89f8-1c7e18a0b6ed")
  private val userApprovedForSyncing = TestData.loggedInUser(uuid = userUuid, loggedInStatus = LOGGED_IN, status = ApprovedForSyncing)
  private val userWaitingForApproval = TestData.loggedInUser(uuid = userUuid, loggedInStatus = LOGGED_IN, status = WaitingForApproval)
  private val userPendingVerification = userApprovedForSyncing.copy(loggedInStatus = OTP_REQUESTED)

  private lateinit var controller: PatientsScreenController
  private lateinit var controllerSubscription: Disposable

  private lateinit var testFixture: MobiusTestFixture<PatientsModel, PatientsEvent, PatientsEffect>

  @Before
  fun setUp() {
    val uiRenderer = PatientsUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = PatientsModel.create(),
        init = PatientsInit(),
        update = PatientsUpdate(),
        effectHandler = PatientsEffectHandler().build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  @After
  fun tearDown() {
    controllerSubscription.dispose()
    testFixture.dispose()
  }

  @Test
  fun `when new patient is clicked then patient search screen should open`() {
    setupController()

    // when
    uiEvents.onNext(NewPatientClicked)

    // then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui).showSimpleVideo()

    verify(ui).openPatientSearchScreen()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when screen is created or resumed then the user's status should refresh regardless of current status`() {
    // when
    setupController()

    // then
    verify(refreshCurrentUser).refresh()

    clearInvocations(refreshCurrentUser)

    // when
    uiEvents.onNext(Resumed(null))

    // then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui).showSimpleVideo()
    verifyNoMoreInteractions(ui)

    verify(refreshCurrentUser).refresh()
  }

  @Test
  fun `when the user is awaiting approval after registration, then the waiting approval status should be shown`() {
    // when
    setupController(user = userWaitingForApproval)

    // then
    verify(ui).hideSyncIndicator()
    verify(ui).showSimpleVideo()

    verify(ui).showUserStatusAsWaiting()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user is awaiting approval after resetting the PIN, then the waiting approval status should be shown`() {
    // when
    val user = userWaitingForApproval.copy(loggedInStatus = RESET_PIN_REQUESTED)
    setupController(user = user)

    // then
    verify(ui).hideSyncIndicator()
    verify(ui).showSimpleVideo()

    verify(ui).showUserStatusAsWaiting()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user is approved for syncing, then the waiting approval status should not be shown`() {
    // when
    setupController(user = userApprovedForSyncing)

    // then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui).showSimpleVideo()

    verify(ui, never()).showUserStatusAsWaiting()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user has been approved within the last 24h and has not dismissed the approval status, then the approval status should be shown`() {
    // when
    setupController(
        user = userApprovedForSyncing,
        approvalStatusApprovedAt = dateAsInstant.minus(23, ChronoUnit.HOURS)
    )

    // then
    verify(ui).showSyncIndicator()
    verify(ui).showSimpleVideo()

    verify(ui).showUserStatusAsApproved()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user has been approved within the last 24h and has dismissed the approval status, then the approval status should not be shown`() {
    // when
    setupController(
        user = userApprovedForSyncing,
        approvalStatusApprovedAt = dateAsInstant.minus(23, ChronoUnit.HOURS),
        hasUserDismissedApprovedStatus = true
    )

    // then
    verify(ui).showSyncIndicator()
    verify(ui).showSimpleVideo()
    verify(ui).hideUserAccountStatus()

    verify(ui, never()).showUserStatusAsApproved()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user has been approved before the last 24h and has not dismissed the approval status, then the approval status should not be shown`() {
    // when
    setupController(
        user = userApprovedForSyncing,
        hasUserDismissedApprovedStatus = false,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // then
    verify(ui).showSyncIndicator()
    verify(ui).showSimpleVideo()
    verify(ui).hideUserAccountStatus()

    verify(ui, never()).showUserStatusAsApproved()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when checking the user's status fails with any error then the error should be silently swallowed`() {
    // when
    setupController(
        user = userWaitingForApproval,
        refreshCurrentUserCompletable = Completable.error(SocketTimeoutException())
    )

    // then
    verify(refreshCurrentUser).refresh()
    verify(approvalStatusApprovedAtPreference).set(any())

    verify(ui).showUserStatusAsWaiting()
    verify(ui).hideSyncIndicator()
    verify(ui).showSimpleVideo()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user dismisses the approved status then the status should be hidden`() {
    // when
    setupController(approvalStatusApprovedAt = dateAsInstant.minus(23, ChronoUnit.HOURS))
    uiEvents.onNext(UserApprovedStatusDismissed())

    // then
    verify(hasUserDismissedApprovedStatusPreference).set(true)

    verify(ui).showUserStatusAsApproved()
    verify(ui).showSyncIndicator()
    verify(ui).showSimpleVideo()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when an approved user is awaiting sms verification, the pending SMS verification status must be shown`() {
    // when
    setupController(
        user = userPendingVerification,
        hasUserDismissedApprovedStatus = true,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // then
    verify(ui).hideSyncIndicator()
    verify(ui).showSimpleVideo()

    verify(ui).showUserStatusAsPendingVerification()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when an approved user has completed sms verification, the verification status must be shown`() {
    // when
    setupController(
        hasUserDismissedApprovedStatus = true,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui).showSimpleVideo()

    verify(ui, never()).showUserStatusAsPendingVerification()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when an approved user is pending OTP verification, the verification status must not be hidden`() {
    // when
    setupController(
        user = userPendingVerification,
        hasUserDismissedApprovedStatus = true,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // then
    verify(ui).showUserStatusAsPendingVerification()
    verify(ui).hideSyncIndicator()
    verify(ui).showSimpleVideo()

    verify(ui, never()).hideUserAccountStatus()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when an approved user has finished OTP verification, the verification status must be hidden`() {
    // when
    setupController(
        hasUserDismissedApprovedStatus = true,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // then
    verify(ui).showSyncIndicator()
    verify(ui).hideUserAccountStatus()
    verify(ui).showSimpleVideo()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when a waiting for approval user is pending OTP verification, the verification status must not be hidden`() {
    // when
    val user = userWaitingForApproval.copy(loggedInStatus = OTP_REQUESTED)
    setupController(
        user = user,
        hasUserDismissedApprovedStatus = true,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // then
    verify(ui).showUserStatusAsWaiting()
    verify(ui).hideSyncIndicator()
    verify(ui).showSimpleVideo()

    verify(ui, never()).hideUserAccountStatus()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when a waiting for approval user has finished OTP verification, the verification status not must be hidden`() {
    // when
    setupController(
        user = userWaitingForApproval,
        hasUserDismissedApprovedStatus = true,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // then
    verify(ui).showUserStatusAsWaiting()
    verify(ui).hideSyncIndicator()
    verify(ui).showSimpleVideo()

    verify(ui, never()).hideUserAccountStatus()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when a user is verified for login, the account status status must be hidden`() {
    // given
    val user = userPendingVerification
    val userAfterLoggingIn = userPendingVerification.copy(loggedInStatus = LOGGED_IN)

    val userSubject = PublishSubject.create<Optional<User>>()
    setupControllerWithUserStream(
        userStream = userSubject,
        hasUserDismissedApprovedStatus = true,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // when
    userSubject.onNext(user.toOptional())

    //then
    verify(ui).showSimpleVideo()
    verify(ui).hideSyncIndicator()
    verify(ui).showUserStatusAsPendingVerification()
    verify(ui, never()).hideUserAccountStatus()
    verifyNoMoreInteractions(ui)
    clearInvocations(ui)

    // when
    userSubject.onNext(userAfterLoggingIn.toOptional())

    // then
    verify(ui).showSyncIndicator()
    verify(ui).hideUserAccountStatus()
    verifyNoMoreInteractions(ui)
    clearInvocations(ui)

    // when
    userSubject.onNext(userAfterLoggingIn.toOptional())
    verify(ui).hideUserAccountStatus()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user decides to enter the login code manually, the enter otp screen must be opened`() {
    setupController()

    // when
    uiEvents.onNext(PatientsEnterCodeManuallyClicked())

    // then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSimpleVideo()
    verify(ui).showSyncIndicator()
    verify(ui).openEnterCodeManuallyScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user clicks scan card id button and the camera permission is granted, open the scan camera screen`() {
    setupController()

    // when
    uiEvents.onNext(ScanCardIdButtonClicked(permission = Just(GRANTED)))

    // then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSimpleVideo()
    verify(ui).showSyncIndicator()
    verify(ui).openScanSimpleIdCardScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user clicks scan card id button and the camera permission is denied, do not open the scan camera screen`() {
    setupController()

    // when
    uiEvents.onNext(ScanCardIdButtonClicked(permission = Just(DENIED)))

    // then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSimpleVideo()
    verify(ui).showSyncIndicator()
    verify(ui, never()).openScanSimpleIdCardScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when an app update is available and the app update dialog has not been shown on the current date, show the app update dialog`() {
    // when
    setupController(
        appUpdateDialogShownAt = dateAsInstant.minusMillis(1),
        appUpdateState = ShowAppUpdate
    )

    // then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSimpleVideo()
    verify(ui).showSyncIndicator()
    verify(ui).showAppUpdateDialog()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when an app update is available and the app update dialog has been shown on the current date, do not show the app update dialog`() {
    // when
    setupController(appUpdateState = ShowAppUpdate)

    // then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSimpleVideo()
    verify(ui).showSyncIndicator()
    verify(ui, never()).showAppUpdateDialog()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when an app update is not available, do not show the app update dialog`() {
    // when
    setupController(
        appUpdateDialogShownAt = dateAsInstant.minusMillis(1),
        appUpdateState = DontShowAppUpdate
    )

    // then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSimpleVideo()
    verify(ui).showSyncIndicator()
    verify(ui, never()).showAppUpdateDialog()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when check for app update fails, do not show the app update dialog`() {
    // when
    setupController(
        appUpdateDialogShownAt = dateAsInstant.minusMillis(1),
        appUpdateState = AppUpdateStateError(RuntimeException())
    )

    // then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSimpleVideo()
    verify(ui).showSyncIndicator()
    verify(ui, never()).showAppUpdateDialog()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when screen is created then display simple video if patient registered count is less than 10`() {
    //when
    setupController(numberOfPatientsRegistered = 9)

    //then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSimpleVideo()
    verify(ui).showSyncIndicator()
    verify(ui).showSimpleVideo()
    verify(ui, never()).showIllustration()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when screen is created then display illustration if patient registered count is at least 10`() {
    //when
    setupController(numberOfPatientsRegistered = 10)

    //then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui, never()).showSimpleVideo()
    verify(ui).showIllustration()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when simple video is clicked then open the video in youtube`() {
    setupController()

    //when
    uiEvents.onNext(SimpleVideoClicked)

    //then
    verify(ui).hideUserAccountStatus()
    verify(ui).showSimpleVideo()
    verify(ui).showSyncIndicator()
    verify(ui).openYouTubeLinkForSimpleVideo()
    verifyNoMoreInteractions(ui)
  }

  private fun setupController(
      user: User = userApprovedForSyncing,
      numberOfPatientsRegistered: Int = 0,
      hasUserDismissedApprovedStatus: Boolean = false,
      approvalStatusApprovedAt: Instant = dateAsInstant.minus(1, ChronoUnit.DAYS),
      refreshCurrentUserCompletable: Completable = Completable.complete(),
      appUpdateDialogShownAt: Instant = dateAsInstant,
      appUpdateState: AppUpdateState = DontShowAppUpdate
  ) {
    createController()
    setupStubs(
        userStream = Observable.just(user.toOptional()),
        refreshCurrentUserCompletable = refreshCurrentUserCompletable,
        numberOfPatientsRegistered = numberOfPatientsRegistered,
        hasUserDismissedApprovedStatus = hasUserDismissedApprovedStatus,
        approvalStatusApprovedAt = approvalStatusApprovedAt,
        appUpdateDialogShownAt = appUpdateDialogShownAt,
        appUpdateStream = Observable.just(appUpdateState)
    )
    activateUi()
  }

  private fun setupControllerWithUserStream(
      userStream: Observable<Optional<User>>,
      numberOfPatientsRegistered: Int = 0,
      hasUserDismissedApprovedStatus: Boolean = false,
      approvalStatusApprovedAt: Instant = dateAsInstant.minus(1, ChronoUnit.DAYS),
      refreshCurrentUserCompletable: Completable = Completable.complete(),
      appUpdateDialogShownAt: Instant = dateAsInstant,
      appUpdateState: AppUpdateState = DontShowAppUpdate
  ) {
    createController()
    setupStubs(
        userStream = userStream,
        refreshCurrentUserCompletable = refreshCurrentUserCompletable,
        numberOfPatientsRegistered = numberOfPatientsRegistered,
        hasUserDismissedApprovedStatus = hasUserDismissedApprovedStatus,
        approvalStatusApprovedAt = approvalStatusApprovedAt,
        appUpdateDialogShownAt = appUpdateDialogShownAt,
        appUpdateStream = Observable.just(appUpdateState)
    )
    activateUi()
  }

  private fun createController() {
    controller = PatientsScreenController(
        userSession = userSession,
        checkAppUpdate = checkAppUpdate,
        utcClock = utcClock,
        userClock = userClock,
        refreshCurrentUser = refreshCurrentUser,
        schedulersProvider = TrampolineSchedulersProvider(),
        approvalStatusUpdatedAtPref = approvalStatusApprovedAtPreference,
        hasUserDismissedApprovedStatusPref = hasUserDismissedApprovedStatusPreference,
        appUpdateDialogShownAtPref = appUpdateDialogShownPref,
        numberOfPatientsRegisteredPref = numberOfPatientsRegisteredPreference
    )
  }

  private fun setupStubs(
      userStream: Observable<Optional<User>>,
      refreshCurrentUserCompletable: Completable,
      numberOfPatientsRegistered: Int,
      hasUserDismissedApprovedStatus: Boolean,
      approvalStatusApprovedAt: Instant,
      appUpdateDialogShownAt: Instant,
      appUpdateStream: Observable<AppUpdateState>
  ) {
    whenever(userSession.loggedInUser()).doReturn(userStream)
    whenever(userSession.canSyncData()).doReturn(userStream.map { if (it is Just) it.value.canSyncData else false })
    whenever(refreshCurrentUser.refresh()).doReturn(refreshCurrentUserCompletable)
    whenever(checkAppUpdate.listen()).doReturn(appUpdateStream)
    whenever(numberOfPatientsRegisteredPreference.get()).doReturn(numberOfPatientsRegistered)
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(hasUserDismissedApprovedStatus))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(hasUserDismissedApprovedStatus)
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(approvalStatusApprovedAt)
    whenever(appUpdateDialogShownPref.get()).doReturn(appUpdateDialogShownAt)
  }

  private fun activateUi() {
    testFixture.start()

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(ui) }

    uiEvents.onNext(ScreenCreated())
  }
}
