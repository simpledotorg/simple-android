package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.activity.ActivityLifecycle.Resumed
import org.simple.clinic.appupdate.AppUpdateState
import org.simple.clinic.appupdate.AppUpdateState.AppUpdateStateError
import org.simple.clinic.appupdate.AppUpdateState.DontShowAppUpdate
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
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.net.SocketTimeoutException
import java.util.UUID

class PatientsScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen: PatientsScreen = mock()
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

  private val canSyncStream = PublishSubject.create<Boolean>()
  private val appUpdatesStream = PublishSubject.create<AppUpdateState>()

  private val userUuid = UUID.fromString("df863f94-a6f9-4409-89f8-1c7e18a0b6ed")
  private val userApprovedForSyncing = TestData.loggedInUser(uuid = userUuid, loggedInStatus = LOGGED_IN, status = ApprovedForSyncing)
  private val userWaitingForApproval = TestData.loggedInUser(uuid = userUuid, loggedInStatus = LOGGED_IN, status = WaitingForApproval)
  private val userPendingVerification = userApprovedForSyncing.copy(loggedInStatus = OTP_REQUESTED)

  private lateinit var controller: PatientsScreenController
  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when new patient is clicked then patient search screen should open`() {
    setupController()

    // when
    uiEvents.onNext(NewPatientClicked)

    // then
    verify(screen).openPatientSearchScreen()
  }

  @Test
  fun `when screen is created or resumed then the user's status should refresh regardless of current status`() {
    // given
    whenever(refreshCurrentUser.refresh()).doReturn(Completable.complete())
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(Instant.now())
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(false)

    // when
    setupController()

    // then
    verify(refreshCurrentUser).refresh()

    clearInvocations(refreshCurrentUser)

    // when
    uiEvents.onNext(Resumed(null))

    // then
    verify(refreshCurrentUser).refresh()
  }

  @Test
  fun `when the user is awaiting approval after registration, then the waiting approval status should be shown`() {
    // given
    whenever(userSession.canSyncData()).doReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(false)

    // when
    setupController(user = userWaitingForApproval)

    // then
    verify(screen).showUserStatusAsWaiting()
  }

  @Test
  fun `when the user is awaiting approval after resetting the PIN, then the waiting approval status should be shown`() {
    // given
    whenever(userSession.canSyncData()).doReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(false)

    // when
    val user = userWaitingForApproval.copy(loggedInStatus = RESET_PIN_REQUESTED)
    setupController(user = user)

    // then
    verify(screen).showUserStatusAsWaiting()
  }

  @Test
  fun `when the user is approved for syncing, then the waiting approval status should not be shown`() {
    // given
    whenever(userSession.canSyncData()).doReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(false)
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(dateAsInstant.minus(Duration.ofDays(2)))

    // when
    setupController()

    // then
    verify(screen, never()).showUserStatusAsWaiting()
  }

  @Test
  fun `when the user has been approved within the last 24h and has not dismissed the approval status, then the approval status should be shown`() {
    // given
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(dateAsInstant.minus(23, ChronoUnit.HOURS))
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(false)

    // when
    setupController()

    // then
    verify(screen).showUserStatusAsApproved()
  }

  @Test
  fun `when the user has been approved within the last 24h and has dismissed the approval status, then the approval status should not be shown`() {
    // when
    setupController(
        approvalStatusApprovedAt = dateAsInstant.minus(23, ChronoUnit.HOURS),
        hasUserDismissedApprovedStatus = true
    )

    // then
    verify(screen, never()).showUserStatusAsApproved()
  }

  @Test
  fun `when the user has been approved before the last 24h and has not dismissed the approval status, then the approval status should not be shown`() {
    // when
    setupController(
        hasUserDismissedApprovedStatus = false,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // then
    verify(screen, never()).showUserStatusAsApproved()
  }

  @Test
  fun `when checking the user's status fails with any error then the error should be silently swallowed`() {
    // given
    whenever(userSession.canSyncData()).doReturn(Observable.never())
    whenever(refreshCurrentUser.refresh()).doReturn(Completable.error(SocketTimeoutException()))
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(false)

    // when
    setupController(user = userWaitingForApproval)

    // then
    verify(refreshCurrentUser).refresh()
    verify(approvalStatusApprovedAtPreference).set(any())
  }

  @Test
  fun `when the user dismisses the approved status then the status should be hidden`() {
    // given
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(dateAsInstant.minus(23, ChronoUnit.HOURS))
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(false)

    // when
    setupController()
    uiEvents.onNext(UserApprovedStatusDismissed())

    // then
    verify(hasUserDismissedApprovedStatusPreference).set(true)
  }

  @Test
  fun `when an approved user is awaiting sms verification, the pending SMS verification status must be shown`() {
    // given
    whenever(userSession.canSyncData()).doReturn(Observable.never())

    // when
    setupController(
        user = userPendingVerification,
        hasUserDismissedApprovedStatus = true,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // then
    verify(screen).showUserStatusAsPendingVerification()
  }

  @Test
  fun `when an approved user has completed sms verification, the verification status must be shown`() {
    // given
    whenever(userSession.canSyncData()).doReturn(Observable.never())

    // when
    setupController(
        hasUserDismissedApprovedStatus = true,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // then
    verify(screen, never()).showUserStatusAsPendingVerification()
  }

  @Test
  fun `when an approved user is pending OTP verification, the verification status must not be hidden`() {
    // given
    whenever(userSession.canSyncData()).doReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(true))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(true)
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(dateAsInstant.minus(25, ChronoUnit.HOURS))

    // when
    setupController(user = userPendingVerification)

    // then
    verify(screen, never()).hideUserAccountStatus()
  }

  @Test
  fun `when an approved user has finished OTP verification, the verification status must be hidden`() {
    // given
    whenever(userSession.canSyncData()).doReturn(Observable.never())

    // when
    setupController(
        hasUserDismissedApprovedStatus = true,
        approvalStatusApprovedAt = dateAsInstant.minus(25, ChronoUnit.HOURS)
    )

    // then
    verify(screen).hideUserAccountStatus()
  }

  @Test
  fun `when a waiting for approval user is pending OTP verification, the verification status must not be hidden`() {
    // given
    whenever(userSession.canSyncData()).doReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(true))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(true)
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(dateAsInstant.minus(25, ChronoUnit.HOURS))

    // when
    val user = userWaitingForApproval.copy(loggedInStatus = OTP_REQUESTED)
    setupController(user = user)

    // then
    verify(screen, never()).hideUserAccountStatus()
  }

  @Test
  fun `when a waiting for approval user has finished OTP verification, the verification status not must be hidden`() {
    // given
    whenever(userSession.canSyncData()).doReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(true))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(true)
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(dateAsInstant.minus(25, ChronoUnit.HOURS))

    // when
    setupController(user = userWaitingForApproval)

    // then
    verify(screen, never()).hideUserAccountStatus()
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
    verify(screen, never()).hideUserAccountStatus()

    // when
    userSubject.onNext(userAfterLoggingIn.toOptional())

    // then
    verify(screen).hideUserAccountStatus()
    clearInvocations(screen)

    // when
    userSubject.onNext(userAfterLoggingIn.toOptional())
    verify(screen).hideUserAccountStatus()
  }

  @Test
  fun `when the user decides to enter the login code manually, the enter otp screen must be opened`() {
    setupController()

    // when
    uiEvents.onNext(PatientsEnterCodeManuallyClicked())

    // then
    verify(screen).openEnterCodeManuallyScreen()
  }

  @Test
  fun `when the user clicks scan card id button and the camera permission is granted, open the scan camera screen`() {
    setupController()

    // when
    uiEvents.onNext(ScanCardIdButtonClicked(permission = Just(GRANTED)))

    // then
    verify(screen).openScanSimpleIdCardScreen()
  }

  @Test
  fun `when the user clicks scan card id button and the camera permission is denied, do not open the scan camera screen`() {
    setupController()

    // when
    uiEvents.onNext(ScanCardIdButtonClicked(permission = Just(DENIED)))

    // then
    verify(screen, never()).openScanSimpleIdCardScreen()
  }

  @Test
  fun `sync indicator should be visible only when user is approved for syncing`() {
    // given
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(false)

    setupController(user = userWaitingForApproval)

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
  fun `when an app update is available and the app update dialog has not been shown on the current date, show the app update dialog`() {
    // given
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(dateAsInstant)
    whenever(appUpdateDialogShownPref.get()).doReturn(dateAsInstant.minusMillis(1))

    // when
    setupController()
    appUpdatesStream.onNext(AppUpdateState.ShowAppUpdate)

    // then
    verify(screen).showAppUpdateDialog()
  }

  @Test
  fun `when an app update is available and the app update dialog has been shown on the current date, show the app update dialog`() {
    // given
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(dateAsInstant)
    whenever(appUpdateDialogShownPref.get()).doReturn(dateAsInstant)

    // when
    setupController()
    appUpdatesStream.onNext(AppUpdateState.ShowAppUpdate)

    // then
    verify(screen, never()).showAppUpdateDialog()
  }

  @Test
  fun `when an app update is not available, do not show the app update dialog`() {
    // given
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(dateAsInstant)
    whenever(appUpdateDialogShownPref.get()).doReturn(dateAsInstant.minusMillis(1))

    // when
    setupController()
    appUpdatesStream.onNext(DontShowAppUpdate)

    // then
    verify(screen, never()).showAppUpdateDialog()
  }

  @Test
  fun `when check for app update fails, do not show the app update dialog`() {
    // given
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(dateAsInstant)
    whenever(appUpdateDialogShownPref.get()).doReturn(dateAsInstant.minusMillis(1))

    // when
    setupController()
    appUpdatesStream.onNext(AppUpdateStateError(RuntimeException()))

    // then
    verify(screen, never()).showAppUpdateDialog()
  }

  @Test
  fun `when screen is created then display simple video if patient registered count is less than 10`() {
    //given
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(dateAsInstant)
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))
    whenever(numberOfPatientsRegisteredPreference.get()).doReturn(9)

    //when
    setupController()

    //then
    verify(screen).showSimpleVideo()
    verify(screen, never()).showIllustration()
  }

  @Test
  fun `when screen is created then display illustration if patient registered count is at least 10`() {
    //given
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(dateAsInstant)
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(false))

    //when
    setupController(numberOfPatientsRegistered = 10)

    //then
    verify(screen, never()).showSimpleVideo()
    verify(screen).showIllustration()
  }

  @Test
  fun `when simple video is clicked then open the video in youtube`() {
    setupController()

    //when
    uiEvents.onNext(SimpleVideoClicked)

    //then
    verify(screen).openYouTubeLinkForSimpleVideo()
  }

  private fun setupController(
      user: User = userApprovedForSyncing,
      numberOfPatientsRegistered: Int = 0,
      hasUserDismissedApprovedStatus: Boolean = false,
      approvalStatusApprovedAt: Instant = dateAsInstant
  ) {
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

    whenever(userSession.loggedInUser()).doReturn(Observable.just(user.toOptional()))
    whenever(userSession.canSyncData()).doReturn(canSyncStream)
    whenever(refreshCurrentUser.refresh()).doReturn(Completable.complete())
    whenever(checkAppUpdate.listen()).doReturn(appUpdatesStream)
    whenever(numberOfPatientsRegisteredPreference.get()).doReturn(numberOfPatientsRegistered)
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(hasUserDismissedApprovedStatus))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(hasUserDismissedApprovedStatus)
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(approvalStatusApprovedAt)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    uiEvents.onNext(ScreenCreated())
  }

  private fun setupControllerWithUserStream(
      userStream: Observable<Optional<User>>,
      numberOfPatientsRegistered: Int = 0,
      hasUserDismissedApprovedStatus: Boolean = false,
      approvalStatusApprovedAt: Instant = dateAsInstant
  ) {
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

    whenever(userSession.loggedInUser()).doReturn(userStream)
    whenever(userSession.canSyncData()).doReturn(canSyncStream)
    whenever(refreshCurrentUser.refresh()).doReturn(Completable.complete())
    whenever(checkAppUpdate.listen()).doReturn(appUpdatesStream)
    whenever(numberOfPatientsRegisteredPreference.get()).doReturn(numberOfPatientsRegistered)
    whenever(hasUserDismissedApprovedStatusPreference.asObservable()).doReturn(Observable.just(hasUserDismissedApprovedStatus))
    whenever(hasUserDismissedApprovedStatusPreference.get()).doReturn(hasUserDismissedApprovedStatus)
    whenever(approvalStatusApprovedAtPreference.get()).doReturn(approvalStatusApprovedAt)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    uiEvents.onNext(ScreenCreated())
  }
}
