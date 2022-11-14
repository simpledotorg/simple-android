package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.appupdate.AppUpdateNotificationScheduler
import org.simple.clinic.appupdate.AppUpdateState
import org.simple.clinic.appupdate.AppUpdateState.AppUpdateStateError
import org.simple.clinic.appupdate.AppUpdateState.DontShowAppUpdate
import org.simple.clinic.appupdate.AppUpdateState.ShowAppUpdate
import org.simple.clinic.appupdate.CheckAppUpdateAvailability
import org.simple.clinic.platform.util.RuntimePermissionResult.DENIED
import org.simple.clinic.platform.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus.ApprovedForSyncing
import org.simple.clinic.user.UserStatus.WaitingForApproval
import org.simple.clinic.user.refreshuser.RefreshCurrentUser
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.RxErrorsRule
import org.simple.sharedTestCode.util.TestUserClock
import org.simple.sharedTestCode.util.TestUtcClock
import java.net.SocketTimeoutException
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.UUID

class PatientsLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<PatientsTabUi>()
  private val uiActions = mock<PatientsTabUiActions>()
  private val userSession = mock<UserSession>()
  private val approvalStatusApprovedAtPreference = mock<Preference<Instant>>()
  private val hasUserDismissedApprovedStatusPreference = mock<Preference<Boolean>>()
  private val checkAppUpdate = mock<CheckAppUpdateAvailability>()
  private val appUpdateDialogShownPref = mock<Preference<Instant>>()
  private val appUpdateNotificationScheduler = mock<AppUpdateNotificationScheduler>()
  private val drugStockReportLastCheckedAt = mock<Preference<Instant>>()
  private val isDrugStockReportFilled = mock<Preference<Optional<Boolean>>>()

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

  private lateinit var testFixture: MobiusTestFixture<PatientsTabModel, PatientsTabEvent, PatientsTabEffect>

  @Before
  fun setUp() {
    val uiRenderer = PatientsTabUiRenderer(ui, LocalDate.of(2021, 3, 22), false)
    val viewEffectHandler = PatientsTabViewEffectHandler(uiActions)
    val facility = TestData.facility()
    val patientsEffectHandler = PatientsEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        refreshCurrentUser = refreshCurrentUser,
        userSession = userSession,
        utcClock = utcClock,
        userClock = userClock,
        checkAppUpdate = checkAppUpdate,
        appUpdateNotificationScheduler = appUpdateNotificationScheduler,
        hasUserDismissedApprovedStatusPref = hasUserDismissedApprovedStatusPreference,
        appUpdateDialogShownAtPref = appUpdateDialogShownPref,
        approvalStatusUpdatedAtPref = approvalStatusApprovedAtPreference,
        drugStockReminder = mock(),
        drugStockReportLastCheckedAt = drugStockReportLastCheckedAt,
        isDrugStockReportFilled = isDrugStockReportFilled,
        currentFacility = Observable.just(facility),
        viewEffectsConsumer = viewEffectHandler::handle
    )

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = PatientsTabModel.create(),
        init = PatientsInit(
            isNotifyAppUpdateAvailableV2Enabled = false,
            isMonthlyDrugStockReportReminderEnabledInIndia = true,
            isPatientLineListEnabled = false
        ),
        update = PatientsTabUpdate(isNotifyAppUpdateAvailableV2Enabled = false),
        effectHandler = patientsEffectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when new patient is clicked then patient search screen should open`() {
    setupController()

    // when
    uiEvents.onNext(NewPatientClicked)

    // then
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui, times(3)).showIllustration()

    verify(uiActions).openPatientSearchScreen(null)

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
    uiEvents.onNext(ActivityResumed)

    // then
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui, times(3)).showIllustration()
    verifyNoMoreInteractions(ui)

    verify(refreshCurrentUser).refresh()
  }

  @Test
  fun `when the user is awaiting approval after registration, then the waiting approval status should be shown`() {
    // when
    setupController(user = userWaitingForApproval)

    // then
    verify(ui).hideSyncIndicator()
    verify(ui, times(2)).showUserStatusAsWaitingForApproval()
    verify(ui, times(3)).showIllustration()

    verifyNoMoreInteractions(ui)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the user is awaiting approval after resetting the PIN, then the waiting approval status should be shown`() {
    // when
    val user = userWaitingForApproval.copy(loggedInStatus = RESET_PIN_REQUESTED)
    setupController(user = user)

    // then
    verify(ui).hideSyncIndicator()
    verify(ui, times(2)).showUserStatusAsWaitingForApproval()
    verify(ui, times(3)).showIllustration()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user is approved for syncing, then the waiting approval status should not be shown`() {
    // when
    setupController(user = userApprovedForSyncing)

    // then
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui).showSyncIndicator()

    verify(ui, never()).showUserStatusAsWaitingForApproval()
    verify(ui, times(3)).showIllustration()

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
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui, times(3)).showIllustration()
    verify(uiActions).showUserStatusAsApproved()

    verifyNoMoreInteractions(ui)
    verifyNoMoreInteractions(uiActions)
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
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui, times(3)).showIllustration()

    verify(uiActions, never()).showUserStatusAsApproved()

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
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui, times(3)).showIllustration()

    verify(uiActions, never()).showUserStatusAsApproved()

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
    verify(approvalStatusApprovedAtPreference).set(dateAsInstant)

    verify(ui, times(2)).showUserStatusAsWaitingForApproval()
    verify(ui).hideSyncIndicator()
    verify(ui, times(3)).showIllustration()
    verifyNoMoreInteractions(ui)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the user dismisses the approved status then the status should be hidden`() {
    // when
    setupController(approvalStatusApprovedAt = dateAsInstant.minus(23, ChronoUnit.HOURS))
    uiEvents.onNext(UserApprovedStatusDismissed())

    // then
    verify(hasUserDismissedApprovedStatusPreference).set(true)

    verify(uiActions).showUserStatusAsApproved()
    verify(ui).showSyncIndicator()
    verify(ui, times(3)).showIllustration()
    verify(ui, times(2)).hideUserAccountStatus()
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
    verify(ui, times(2)).showUserStatusAsPendingVerification()
    verify(ui, times(3)).showIllustration()

    verifyNoMoreInteractions(ui)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when an approved user has completed sms verification, the verification status must be shown`() {
    // when
    setupController(
        hasUserDismissedApprovedStatus = true,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // then
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui, times(3)).showIllustration()

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
    verify(ui, times(2)).showUserStatusAsPendingVerification()
    verify(ui).hideSyncIndicator()
    verify(ui, times(3)).showIllustration()

    verify(uiActions, never()).hideUserAccountStatus()

    verifyNoMoreInteractions(ui)
    verifyNoMoreInteractions(uiActions)
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
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui, times(3)).showIllustration()

    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when a waiting for approval user is pending OTP verification, the pending verification status must be shown`() {
    // when
    val user = userWaitingForApproval.copy(loggedInStatus = OTP_REQUESTED)
    setupController(
        user = user,
        hasUserDismissedApprovedStatus = true,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // then
    verify(ui, times(2)).showUserStatusAsPendingVerification()
    verify(ui).hideSyncIndicator()
    verify(ui, times(3)).showIllustration()

    verify(uiActions, never()).hideUserAccountStatus()

    verifyNoMoreInteractions(ui)
    verifyNoMoreInteractions(uiActions)
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
    verify(ui, times(2)).showUserStatusAsWaitingForApproval()
    verify(ui).hideSyncIndicator()
    verify(ui, times(3)).showIllustration()
    verify(uiActions, never()).hideUserAccountStatus()

    verifyNoMoreInteractions(ui)
    verifyNoMoreInteractions(uiActions)
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
    verify(ui).hideSyncIndicator()
    verify(ui).showUserStatusAsPendingVerification()
    verify(uiActions, never()).hideUserAccountStatus()
    verify(ui, times(3)).showIllustration()
    verifyNoMoreInteractions(ui)
    verifyNoMoreInteractions(uiActions)
    clearInvocations(ui)
    clearInvocations(uiActions)

    // when
    userSubject.onNext(userAfterLoggingIn.toOptional())

    // then
    verify(ui).showSyncIndicator()
    verify(ui).hideUserAccountStatus()
    verify(ui).showIllustration()
    verifyNoMoreInteractions(ui)
    verifyNoMoreInteractions(uiActions)
    clearInvocations(ui)
    clearInvocations(uiActions)

    // when
    userSubject.onNext(userAfterLoggingIn.toOptional())
    verify(ui).hideUserAccountStatus()
    verify(ui).showIllustration()
    verifyNoMoreInteractions(ui)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the user decides to enter the login code manually, the enter otp screen must be opened`() {
    setupController()

    // when
    uiEvents.onNext(PatientsEnterCodeManuallyClicked())

    // then
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui, times(3)).showIllustration()
    verify(uiActions).openEnterCodeManuallyScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user clicks scan card id button and the camera permission is granted, open the scan camera screen`() {
    setupController()

    // when
    uiEvents.onNext(ScanCardIdButtonClicked(permission = Optional.of(GRANTED)))

    // then
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui, times(3)).showIllustration()
    verify(uiActions).openScanSimpleIdCardScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user clicks scan card id button and the camera permission is denied, do not open the scan camera screen`() {
    setupController()

    // when
    uiEvents.onNext(ScanCardIdButtonClicked(permission = Optional.of(DENIED)))

    // then
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui, times(3)).showIllustration()
    verify(uiActions, never()).openScanSimpleIdCardScreen()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when an app update is available and the app update dialog has not been shown on the current date, show the app update dialog`() {
    // when
    setupController(
        appUpdateDialogShownAt = dateAsInstant.minusMillis(1),
        appUpdateState = ShowAppUpdate(appUpdateNudgePriority = null, appStaleness = null)
    )

    // then
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui, times(3)).showIllustration()
    verify(uiActions).showAppUpdateDialog()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when an app update is available and the app update dialog has been shown on the current date, do not show the app update dialog`() {
    // when
    setupController(appUpdateState = ShowAppUpdate(appUpdateNudgePriority = null, appStaleness = null))

    // then
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui, times(3)).showIllustration()
    verify(uiActions, never()).showAppUpdateDialog()
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
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(uiActions, never()).showAppUpdateDialog()
    verify(ui, times(3)).showIllustration()
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
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui, times(3)).showIllustration()
    verify(uiActions, never()).showAppUpdateDialog()
    verifyNoMoreInteractions(ui)
    //    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when screen is created then display illustration`() {
    //when
    setupController()

    //then
    verify(ui, times(2)).hideUserAccountStatus()
    verify(ui).showSyncIndicator()
    verify(ui, times(3)).showIllustration()
    verifyNoMoreInteractions(ui)
  }

  private fun setupController(
      user: User = userApprovedForSyncing,
      hasUserDismissedApprovedStatus: Boolean = false,
      approvalStatusApprovedAt: Instant = dateAsInstant.minus(1, ChronoUnit.DAYS),
      refreshCurrentUserCompletable: Completable = Completable.complete(),
      appUpdateDialogShownAt: Instant = dateAsInstant,
      appUpdateState: AppUpdateState = DontShowAppUpdate
  ) {
    setupStubs(
        userStream = Observable.just(user.toOptional()),
        refreshCurrentUserCompletable = refreshCurrentUserCompletable,
        hasUserDismissedApprovedStatus = hasUserDismissedApprovedStatus,
        approvalStatusApprovedAt = approvalStatusApprovedAt,
        appUpdateDialogShownAt = appUpdateDialogShownAt,
        appUpdateStream = Observable.just(appUpdateState)
    )
    activateUi()
  }

  private fun setupControllerWithUserStream(
      userStream: Observable<Optional<User>>,
      hasUserDismissedApprovedStatus: Boolean = false,
      approvalStatusApprovedAt: Instant = dateAsInstant.minus(1, ChronoUnit.DAYS),
      refreshCurrentUserCompletable: Completable = Completable.complete(),
      appUpdateDialogShownAt: Instant = dateAsInstant,
      appUpdateState: AppUpdateState = DontShowAppUpdate
  ) {
    setupStubs(
        userStream = userStream,
        refreshCurrentUserCompletable = refreshCurrentUserCompletable,
        hasUserDismissedApprovedStatus = hasUserDismissedApprovedStatus,
        approvalStatusApprovedAt = approvalStatusApprovedAt,
        appUpdateDialogShownAt = appUpdateDialogShownAt,
        appUpdateStream = Observable.just(appUpdateState)
    )
    activateUi()
  }

  private fun setupStubs(
      userStream: Observable<Optional<User>>,
      refreshCurrentUserCompletable: Completable,
      hasUserDismissedApprovedStatus: Boolean,
      approvalStatusApprovedAt: Instant,
      appUpdateDialogShownAt: Instant,
      appUpdateStream: Observable<AppUpdateState>
  ) {
    whenever(userSession.loggedInUser()).doReturn(userStream)
    whenever(refreshCurrentUser.refresh()).doReturn(refreshCurrentUserCompletable)
    whenever(checkAppUpdate.listen()).doReturn(appUpdateStream)
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(hasUserDismissedApprovedStatus))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(hasUserDismissedApprovedStatus)
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(approvalStatusApprovedAt)
    whenever(appUpdateDialogShownPref.get()).doReturn(appUpdateDialogShownAt)
    whenever(drugStockReportLastCheckedAt.get()).doReturn(Instant.parse("2018-01-01T00:00:00Z"))
    whenever(isDrugStockReportFilled.get()).doReturn(Optional.of(true))
  }

  private fun activateUi() {
    testFixture.start()
  }
}
