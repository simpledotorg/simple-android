package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.activity.ActivityLifecycle.Resumed
import org.simple.clinic.appupdate.AppUpdateState
import org.simple.clinic.appupdate.CheckAppUpdateAvailability
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.NOT_LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.user.UserStatus.ApprovedForSyncing
import org.simple.clinic.user.UserStatus.DisapprovedForSyncing
import org.simple.clinic.user.UserStatus.WaitingForApproval
import org.simple.clinic.user.refreshuser.RefreshCurrentUser
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import java.net.SocketTimeoutException

@RunWith(JUnitParamsRunner::class)
class PatientsScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen: PatientsScreen = mock()
  private val userSession = mock<UserSession>()
  private val approvalStatusApprovedAt = mock<Preference<Instant>>()
  private val hasUserDismissedApprovedStatus = mock<Preference<Boolean>>()
  private val checkAppUpdate = mock<CheckAppUpdateAvailability>()
  private val appUpdateDialogShownPref = mock<Preference<Instant>>()
  private val utcClock = TestUtcClock()
  private val userClock = TestUserClock()
  private val numberOfPatientsRegisteredPref = mock<Preference<Int>>()
  private val refreshCurrentUser = mock<RefreshCurrentUser>()

  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()
  private lateinit var controller: PatientsScreenController

  private val canSyncStream = PublishSubject.create<Boolean>()
  private val appUpdatesStream = PublishSubject.create<AppUpdateState>()

  @Before
  fun setUp() {
    controller = PatientsScreenController(
        userSession = userSession,
        checkAppUpdate = checkAppUpdate,
        utcClock = utcClock,
        userClock = userClock,
        refreshCurrentUser = refreshCurrentUser,
        schedulersProvider = TrampolineSchedulersProvider(),
        approvalStatusUpdatedAtPref = approvalStatusApprovedAt,
        hasUserDismissedApprovedStatusPref = hasUserDismissedApprovedStatus,
        appUpdateDialogShownAtPref = appUpdateDialogShownPref,
        numberOfPatientsRegisteredPref = numberOfPatientsRegisteredPref
    )

    whenever(userSession.canSyncData()).doReturn(canSyncStream)
    whenever(refreshCurrentUser.refresh()).doReturn(Completable.never())
    whenever(checkAppUpdate.listen()).doReturn(appUpdatesStream)
    whenever(numberOfPatientsRegisteredPref.get()).doReturn(0)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when new patient is clicked then patient search screen should open`() {
    // when
    uiEvents.onNext(NewPatientClicked)

    // then
    verify(screen).openPatientSearchScreen()
  }

  @Test
  @Parameters(method = "params for user status for syncing")
  fun `when screen is created or resumed then the user's status should refresh regardless of current status`(
      status: UserStatus
  ) {
    // given
    val user = TestData.loggedInUser(status = status)
    whenever(userSession.loggedInUser()).doReturn(Observable.just<Optional<User>>(Just(user)))
    whenever(refreshCurrentUser.refresh()).doReturn(Completable.complete())
    whenever(approvalStatusApprovedAt.get()).doReturn(Instant.now())
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).doReturn(false)

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    verify(refreshCurrentUser).refresh()

    clearInvocations(refreshCurrentUser)

    // when
    uiEvents.onNext(Resumed(null))

    // then
    verify(refreshCurrentUser).refresh()
  }

  @Suppress("unused")
  private fun `params for user status for syncing`() =
      listOf(ApprovedForSyncing, DisapprovedForSyncing, WaitingForApproval)

  @Test
  @Parameters(value = [
    "LOGGED_IN|true",
    "RESET_PIN_REQUESTED|true"
  ])
  fun `when the user is awaiting approval then the waiting approval status should be shown`(
      loggedInStatus: LoggedInStatus,
      shouldShowApprovalStatus: Boolean
  ) {
    // given
    val user = TestData.loggedInUser(status = WaitingForApproval, loggedInStatus = loggedInStatus)
    whenever(userSession.loggedInUser()).doReturn(Observable.just<Optional<User>>(Just(user)))
    whenever(userSession.canSyncData()).doReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).doReturn(false)

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    if (shouldShowApprovalStatus) {
      verify(screen).showUserStatusAsWaiting()
    } else {
      verify(screen, never()).showUserStatusAsWaiting()
    }
  }

  @Test
  fun `when the user has been disapproved then the approval status shouldn't be shown`() {
    // given
    val user = TestData.loggedInUser(status = DisapprovedForSyncing, loggedInStatus = LOGGED_IN)
    whenever(userSession.loggedInUser()).doReturn(Observable.just<Optional<User>>(Just(user)))
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).doReturn(false)

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    verify(screen).hideUserAccountStatus()
  }

  @Test
  @Parameters(value = [
    "LOGGED_IN|true|false",
    "LOGGED_IN|false|true",
    "RESET_PIN_REQUESTED|true|false",
    "RESET_PIN_REQUESTED|false|true"
  ]
  )
  fun `when the user has been approved within the last 24h then the approval status should be shown`(
      loggedInStatus: LoggedInStatus,
      hasUserDismissedStatus: Boolean,
      shouldShowApprovedStatus: Boolean
  ) {
    // given
    val user = TestData.loggedInUser(status = ApprovedForSyncing, loggedInStatus = loggedInStatus)
    whenever(userSession.loggedInUser()).doReturn(Observable.just<Optional<User>>(Just(user)))
    whenever(approvalStatusApprovedAt.get()).doReturn(Instant.now().minus(23, ChronoUnit.HOURS))
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(hasUserDismissedStatus))
    whenever(hasUserDismissedApprovedStatus.get()).doReturn(hasUserDismissedStatus)

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    if (shouldShowApprovedStatus) {
      verify(screen).showUserStatusAsApproved()
    } else {
      verify(screen, never()).showUserStatusAsApproved()
    }
  }

  @Test
  @Parameters("true", "false")
  fun `when the user was approved earlier than 24h then the approval status should not be shown`(
      hasUserDismissedStatus: Boolean
  ) {
    // given
    val user = TestData.loggedInUser(status = ApprovedForSyncing)
    whenever(userSession.loggedInUser()).doReturn(Observable.just<Optional<User>>(Just(user)))
    whenever(approvalStatusApprovedAt.get()).doReturn(Instant.now().minus(25, ChronoUnit.HOURS))
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(hasUserDismissedStatus))
    whenever(hasUserDismissedApprovedStatus.get()).doReturn(hasUserDismissedStatus)

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    verify(screen, never()).showUserStatusAsApproved()
  }

  @Test
  fun `when checking the user's status fails with any error then the error should be silently swallowed`() {
    // given
    val user = TestData.loggedInUser(status = WaitingForApproval)
    whenever(userSession.canSyncData()).doReturn(Observable.never())
    whenever(userSession.loggedInUser()).doReturn(Observable.just<Optional<User>>(Just(user)))
    whenever(refreshCurrentUser.refresh()).doReturn(Completable.error(SocketTimeoutException()))
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).doReturn(false)

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    verify(refreshCurrentUser).refresh()
    verify(approvalStatusApprovedAt).set(any())
  }

  @Test
  fun `when the user dismisses the approved status then the status should be hidden`() {
    // given
    val user = TestData.loggedInUser(status = ApprovedForSyncing)
    whenever(userSession.loggedInUser()).doReturn(Observable.just<Optional<User>>(Just(user)))
    whenever(approvalStatusApprovedAt.get()).doReturn(Instant.now().minus(23, ChronoUnit.HOURS))
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).doReturn(false)

    // when
    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(UserApprovedStatusDismissed())

    // then
    verify(hasUserDismissedApprovedStatus).set(true)
  }

  @Test
  @Parameters(method = "params for verification status when when waiting for sms verification")
  fun `when an approved user is awaiting sms verification, the verification status must be shown`(
      userStatus: UserStatus,
      loggedInStatus: LoggedInStatus,
      shouldShowMessage: Boolean
  ) {
    // given
    val user = TestData.loggedInUser(status = userStatus, loggedInStatus = loggedInStatus)
    whenever(userSession.loggedInUser()).doReturn(Observable.just<Optional<User>>(Just(user)))
    whenever(userSession.canSyncData()).doReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(true))
    whenever(hasUserDismissedApprovedStatus.get()).doReturn(true)
    whenever(approvalStatusApprovedAt.get()).doReturn(Instant.now().minus(25, ChronoUnit.HOURS))

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    if (shouldShowMessage) {
      verify(screen).showUserStatusAsPendingVerification()
    } else {
      verify(screen, never()).showUserStatusAsPendingVerification()
    }
  }

  @Suppress("unused")
  private fun `params for verification status when when waiting for sms verification`() =
      listOf(
          listOf(WaitingForApproval, NOT_LOGGED_IN, false),
          listOf(WaitingForApproval, OTP_REQUESTED, false),
          listOf(WaitingForApproval, LOGGED_IN, false),
          listOf(ApprovedForSyncing, NOT_LOGGED_IN, true),
          listOf(ApprovedForSyncing, OTP_REQUESTED, true),
          listOf(ApprovedForSyncing, LOGGED_IN, false),
          listOf(DisapprovedForSyncing, NOT_LOGGED_IN, true),
          listOf(DisapprovedForSyncing, OTP_REQUESTED, true),
          listOf(DisapprovedForSyncing, LOGGED_IN, false)
      )

  @Test
  @Parameters(method = "params for verification status of approved user")
  fun `when an approved user is verified, the verification status must be hidden`(
      userStatus: UserStatus,
      loggedInStatus: LoggedInStatus,
      shouldHideMessage: Boolean
  ) {
    // given
    val user = TestData.loggedInUser(status = userStatus, loggedInStatus = loggedInStatus)
    whenever(userSession.loggedInUser()).doReturn(Observable.just<Optional<User>>(Just(user)))
    whenever(userSession.canSyncData()).doReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(true))
    whenever(hasUserDismissedApprovedStatus.get()).doReturn(true)
    whenever(approvalStatusApprovedAt.get()).doReturn(Instant.now().minus(25, ChronoUnit.HOURS))

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    if (shouldHideMessage) {
      verify(screen).hideUserAccountStatus()
    } else {
      verify(screen, never()).hideUserAccountStatus()
    }
  }

  @Suppress("unused")
  private fun `params for verification status of approved user`() =
      listOf(
          listOf(WaitingForApproval, NOT_LOGGED_IN, false),
          listOf(WaitingForApproval, OTP_REQUESTED, false),
          listOf(WaitingForApproval, LOGGED_IN, false),
          listOf(ApprovedForSyncing, NOT_LOGGED_IN, false),
          listOf(ApprovedForSyncing, OTP_REQUESTED, false),
          listOf(ApprovedForSyncing, LOGGED_IN, true),
          listOf(DisapprovedForSyncing, NOT_LOGGED_IN, false),
          listOf(DisapprovedForSyncing, OTP_REQUESTED, false),
          listOf(DisapprovedForSyncing, LOGGED_IN, true)
      )

  @Test
  @Parameters(
      "OTP_REQUESTED|OTP_REQUESTED|OTP_REQUESTED|false",
      "OTP_REQUESTED|OTP_REQUESTED|LOGGED_IN|true",
      "OTP_REQUESTED|LOGGED_IN|LOGGED_IN|true",
      "LOGGED_IN|LOGGED_IN|LOGGED_IN|true"
  )
  fun `when a user is verified for login, the account status status must be hidden`(
      prevloggedInStatus: LoggedInStatus,
      curLoggedInStatus: LoggedInStatus,
      nextLoggedInStatus: LoggedInStatus,
      shouldHideUserAccountStatus: Boolean
  ) {
    // given
    val user = TestData.loggedInUser(status = ApprovedForSyncing, loggedInStatus = prevloggedInStatus)
    whenever(userSession.loggedInUser()).doReturn(
        Observable.just<Optional<User>>(
            Just(user),
            Just(user.copy(loggedInStatus = curLoggedInStatus)),
            Just(user.copy(loggedInStatus = nextLoggedInStatus)))
    )
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(true))
    whenever(hasUserDismissedApprovedStatus.get()).doReturn(true)
    whenever(approvalStatusApprovedAt.get()).doReturn(Instant.now().minus(25, ChronoUnit.HOURS))

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    if (shouldHideUserAccountStatus) {
      verify(screen, atLeastOnce()).hideUserAccountStatus()
    } else {
      verify(screen, never()).hideUserAccountStatus()
    }
  }

  @Test
  fun `when the user decides to enter the login code manually, the enter otp screen must be opened`() {
    // when
    uiEvents.onNext(PatientsEnterCodeManuallyClicked())

    // then
    verify(screen).openEnterCodeManuallyScreen()
  }

  @Test
  fun `when the user clicks scan card id button and the camera permission is granted, open the scan camera screen`() {
    // when
    uiEvents.onNext(ScanCardIdButtonClicked(permission = Just(GRANTED)))

    // then
    verify(screen).openScanSimpleIdCardScreen()
  }

  @Test
  fun `when the user clicks scan card id button and the camera permission is denied, do not open the scan camera screen`() {
    // when
    uiEvents.onNext(ScanCardIdButtonClicked(permission = Just(DENIED)))

    // then
    verify(screen, never()).openScanSimpleIdCardScreen()
  }

  @Test
  fun `sync indicator should be visible only when user is approved for syncing`() {
    // given
    val user = TestData.loggedInUser(status = WaitingForApproval).toOptional()
    whenever(userSession.loggedInUser()).doReturn(Observable.just(user))
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).doReturn(false)

    uiEvents.onNext(ScreenCreated())

    // when
    canSyncStream.onNext(false)

    // then
    verify(screen).hideSyncIndicator()
    verify(screen, never()).showSyncIndicator()

    // when
    canSyncStream.onNext(true)
    canSyncStream.onNext(true)

    // then
    verify(screen).showSyncIndicator()
  }

  @Test
  @Parameters(method = "params for testing app update dialog")
  fun `when app update is available and the update dialog was not shown for the day, then it should be shown`(
      appUpdateState: AppUpdateState,
      lastAppUpdateDialogShownAt: Instant,
      shouldShow: Boolean
  ) {
    // given
    whenever(userSession.loggedInUser()).doReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(false))
    whenever(appUpdateDialogShownPref.get()).doReturn(lastAppUpdateDialogShownAt)

    // when
    uiEvents.onNext(ScreenCreated())
    appUpdatesStream.onNext(appUpdateState)

    // then
    if (shouldShow) {
      verify(screen).showAppUpdateDialog()
    } else {
      verify(screen, never()).showAppUpdateDialog()
    }
  }

  @Test
  fun `when screen is created then display simple video if patient registered count is less than 10`() {
    //given
    whenever(userSession.loggedInUser()).doReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(false))
    whenever(numberOfPatientsRegisteredPref.get()).doReturn(9)

    //when
    uiEvents.onNext(ScreenCreated())

    //then
    verify(screen).showSimpleVideo()
    verify(screen, never()).showIllustration()
  }

  @Test
  fun `when screen is created then display illustration if patient registered count is exceeds 10`() {
    //given
    whenever(userSession.loggedInUser()).doReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatus.asObservable()).doReturn(Observable.just(false))
    whenever(numberOfPatientsRegisteredPref.get()).doReturn(10)

    //when
    uiEvents.onNext(ScreenCreated())

    //then
    verify(screen, never()).showSimpleVideo()
    verify(screen).showIllustration()
  }

  @Test
  fun `when simple video is clicked then open the video in youtube`() {
    //when
    uiEvents.onNext(SimpleVideoClicked)

    //then
    verify(screen).openYouTubeLinkForSimpleVideo()
  }

  fun `params for testing app update dialog`(): List<Any> {

    fun testCase(
        appUpdateState: AppUpdateState,
        lastAppUpdateDialogShownAt: Instant,
        shouldShow: Boolean
    ) = listOf(appUpdateState, lastAppUpdateDialogShownAt, shouldShow)

    return listOf(
        testCase(
            appUpdateState = AppUpdateState.ShowAppUpdate,
            lastAppUpdateDialogShownAt = Instant.now(utcClock).minus(1, ChronoUnit.DAYS),
            shouldShow = true
        ),
        testCase(
            appUpdateState = AppUpdateState.ShowAppUpdate,
            lastAppUpdateDialogShownAt = Instant.now(utcClock),
            shouldShow = false
        ),
        testCase(
            appUpdateState = AppUpdateState.ShowAppUpdate,
            lastAppUpdateDialogShownAt = Instant.now(utcClock).plus(1, ChronoUnit.DAYS),
            shouldShow = false
        ),
        testCase(
            appUpdateState = AppUpdateState.DontShowAppUpdate,
            lastAppUpdateDialogShownAt = Instant.now(utcClock),
            shouldShow = false
        ),
        testCase(
            appUpdateState = AppUpdateState.DontShowAppUpdate,
            lastAppUpdateDialogShownAt = Instant.now(utcClock).minus(2, ChronoUnit.DAYS),
            shouldShow = false
        ),
        testCase(
            appUpdateState = AppUpdateState.AppUpdateStateError(IllegalStateException()),
            lastAppUpdateDialogShownAt = Instant.now(utcClock),
            shouldShow = false
        ),
        testCase(
            appUpdateState = AppUpdateState.AppUpdateStateError(IllegalStateException()),
            lastAppUpdateDialogShownAt = Instant.now(utcClock).minus(1, ChronoUnit.DAYS),
            shouldShow = false
        )
    )
  }
}
