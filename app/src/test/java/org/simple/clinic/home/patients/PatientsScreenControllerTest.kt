package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.atLeastOnce
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.simple.clinic.patient.PatientConfig
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.User.LoggedInStatus
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.user.UserStatus.APPROVED_FOR_SYNCING
import org.simple.clinic.user.UserStatus.DISAPPROVED_FOR_SYNCING
import org.simple.clinic.user.UserStatus.WAITING_FOR_APPROVAL
import org.simple.clinic.util.Just
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.TheActivityLifecycle
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
  private val dataSync = mock<DataSync>()

  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()
  private lateinit var controller: PatientsScreenController
  private val configEmitter = PublishSubject.create<PatientConfig>()

  private val canSyncStream = PublishSubject.create<Boolean>()

  @Before
  fun setUp() {
    // This is needed because we manually subscribe to the refresh user status
    // operation on the IO thread, which was causing flakiness in this test.
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

    controller = PatientsScreenController(
        userSession = userSession,
        dataSync = dataSync,
        approvalStatusUpdatedAtPref = approvalStatusApprovedAt,
        hasUserDismissedApprovedStatusPref = hasUserDismissedApprovedStatus,
        configProvider = configEmitter
    )

    whenever(userSession.canSyncData()).thenReturn(canSyncStream)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    configEmitter.onNext(PatientConfig(limitOfSearchResults = 1, scanSimpleCardFeatureEnabled = false, recentPatientLimit = 10))
  }

  @Test
  fun `when new patient is clicked then patient search screen should open`() {
    uiEvents.onNext(NewPatientClicked)

    verify(screen).openPatientSearchScreen()
  }

  @Test
  fun `when screen is created and the user is awaiting approval then the approval status should be checked`() {
    val user = PatientMocker.loggedInUser(status = WAITING_FOR_APPROVAL)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.complete())
    whenever(userSession.canSyncData()).thenReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).thenReturn(false)

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(TheActivityLifecycle.Resumed())
    uiEvents.onNext(TheActivityLifecycle.Resumed())

    verify(userSession, times(3)).refreshLoggedInUser()
  }

  @Test
  @Parameters(value = ["APPROVED_FOR_SYNCING", "DISAPPROVED_FOR_SYNCING"])
  fun `when screen is created and the user is not awaiting approval then the user's status should not be checked`(
      status: UserStatus
  ) {
    val user = PatientMocker.loggedInUser(status = status)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.never())
    whenever(approvalStatusApprovedAt.get()).thenReturn(Instant.now())
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).thenReturn(false)

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(TheActivityLifecycle.Resumed())
    uiEvents.onNext(TheActivityLifecycle.Resumed())

    verify(userSession, never()).refreshLoggedInUser()
  }

  @Test
  @Parameters(value = [
    "LOGGED_IN|true",
    "RESET_PIN_REQUESTED|true"
  ])
  fun `when the user is awaiting approval then the waiting approval status should be shown`(
      loggedInStatus: LoggedInStatus,
      shouldShowApprovalStatus: Boolean
  ) {
    val user = PatientMocker.loggedInUser(status = WAITING_FOR_APPROVAL, loggedInStatus = loggedInStatus)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.never())
    whenever(userSession.canSyncData()).thenReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).thenReturn(false)

    uiEvents.onNext(ScreenCreated())

    if (shouldShowApprovalStatus) {
      verify(screen).showUserStatusAsWaiting()
    } else {
      verify(screen, never()).showUserStatusAsWaiting()
    }
  }

  @Test
  fun `when the user has been disapproved then the approval status shouldn't be shown`() {
    val user = PatientMocker.loggedInUser(status = DISAPPROVED_FOR_SYNCING)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).thenReturn(false)

    uiEvents.onNext(ScreenCreated())

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
    val user = PatientMocker.loggedInUser(status = APPROVED_FOR_SYNCING, loggedInStatus = loggedInStatus)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(approvalStatusApprovedAt.get()).thenReturn(Instant.now().minus(23, ChronoUnit.HOURS))
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.just(hasUserDismissedStatus))
    whenever(hasUserDismissedApprovedStatus.get()).thenReturn(hasUserDismissedStatus)

    uiEvents.onNext(ScreenCreated())

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
    val user = PatientMocker.loggedInUser(status = APPROVED_FOR_SYNCING)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.complete())
    whenever(approvalStatusApprovedAt.get()).thenReturn(Instant.now().minus(25, ChronoUnit.HOURS))
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.just(hasUserDismissedStatus))
    whenever(hasUserDismissedApprovedStatus.get()).thenReturn(hasUserDismissedStatus)

    uiEvents.onNext(ScreenCreated())

    verify(screen, never()).showUserStatusAsApproved()
  }

  @Test
  fun `when checking the user's status fails with any error then the error should be silently swallowed`() {
    val user = PatientMocker.loggedInUser(status = WAITING_FOR_APPROVAL)
    whenever(userSession.canSyncData()).thenReturn(Observable.never())
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.error(SocketTimeoutException()))
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).thenReturn(false)

    uiEvents.onNext(ScreenCreated())

    verify(userSession).refreshLoggedInUser()
    verify(approvalStatusApprovedAt).set(any())
  }

  @Test
  fun `when the user dismisses the approved status then the status should be hidden`() {
    val user = PatientMocker.loggedInUser(status = APPROVED_FOR_SYNCING)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(approvalStatusApprovedAt.get()).thenReturn(Instant.now().minus(23, ChronoUnit.HOURS))
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).thenReturn(false)

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(UserApprovedStatusDismissed())

    verify(hasUserDismissedApprovedStatus).set(true)
  }

  @Test
  @Parameters("false", "true")
  fun `when user is refreshed then patient data should be synced if the can sync data flag is set`(
      canUserSyncData: Boolean
  ) {
    whenever(userSession.loggedInUser())
        .thenReturn(Observable.just(PatientMocker.loggedInUser(status = WAITING_FOR_APPROVAL).toOptional()))

    whenever(userSession.canSyncData()).thenReturn(Observable.just(canUserSyncData))

    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.complete())
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).thenReturn(false)
    whenever(approvalStatusApprovedAt.get()).thenReturn(Instant.now())
    whenever(dataSync.sync(null)).thenReturn(Completable.complete())

    uiEvents.onNext(ScreenCreated())

    verify(userSession).refreshLoggedInUser()

    if (canUserSyncData) {
      verify(dataSync).sync(null)
    } else {
      verify(dataSync, never()).sync(null)
    }
  }

  @Test
  @Parameters(
      "WAITING_FOR_APPROVAL|NOT_LOGGED_IN|false",
      "WAITING_FOR_APPROVAL|OTP_REQUESTED|false",
      "WAITING_FOR_APPROVAL|LOGGED_IN|false",
      "APPROVED_FOR_SYNCING|NOT_LOGGED_IN|true",
      "APPROVED_FOR_SYNCING|OTP_REQUESTED|true",
      "APPROVED_FOR_SYNCING|LOGGED_IN|false",
      "DISAPPROVED_FOR_SYNCING|NOT_LOGGED_IN|true",
      "DISAPPROVED_FOR_SYNCING|OTP_REQUESTED|true",
      "DISAPPROVED_FOR_SYNCING|LOGGED_IN|false"
  )
  fun `when an approved user is awaiting sms verification, the verification status must be shown`(
      userStatus: UserStatus,
      loggedInStatus: LoggedInStatus,
      shouldShowMessage: Boolean
  ) {
    val user = PatientMocker.loggedInUser(status = userStatus, loggedInStatus = loggedInStatus)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.complete())
    whenever(userSession.canSyncData()).thenReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.just(true))
    whenever(hasUserDismissedApprovedStatus.get()).thenReturn(true)
    whenever(approvalStatusApprovedAt.get()).thenReturn(Instant.now().minus(25, ChronoUnit.HOURS))

    uiEvents.onNext(ScreenCreated())

    if (shouldShowMessage) {
      verify(screen).showUserStatusAsPendingVerification()
    } else {
      verify(screen, never()).showUserStatusAsPendingVerification()
    }
  }

  @Test
  @Parameters(
      "WAITING_FOR_APPROVAL|NOT_LOGGED_IN|false",
      "WAITING_FOR_APPROVAL|OTP_REQUESTED|false",
      "WAITING_FOR_APPROVAL|LOGGED_IN|false",
      "APPROVED_FOR_SYNCING|NOT_LOGGED_IN|false",
      "APPROVED_FOR_SYNCING|OTP_REQUESTED|false",
      "APPROVED_FOR_SYNCING|LOGGED_IN|true",
      "DISAPPROVED_FOR_SYNCING|NOT_LOGGED_IN|false",
      "DISAPPROVED_FOR_SYNCING|OTP_REQUESTED|false",
      "DISAPPROVED_FOR_SYNCING|LOGGED_IN|true"
  )
  fun `when an approved user is verified, the verification status must be hidden`(
      userStatus: UserStatus,
      loggedInStatus: LoggedInStatus,
      shouldHideMessage: Boolean
  ) {
    val user = PatientMocker.loggedInUser(status = userStatus, loggedInStatus = loggedInStatus)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))
    whenever(userSession.canSyncData()).thenReturn(Observable.never())
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.complete())
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.just(true))
    whenever(hasUserDismissedApprovedStatus.get()).thenReturn(true)
    whenever(approvalStatusApprovedAt.get()).thenReturn(Instant.now().minus(25, ChronoUnit.HOURS))

    uiEvents.onNext(ScreenCreated())

    if (shouldHideMessage) {
      verify(screen).hideUserAccountStatus()
    } else {
      verify(screen, never()).hideUserAccountStatus()
    }
  }

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
    val user = PatientMocker.loggedInUser(status = APPROVED_FOR_SYNCING, loggedInStatus = prevloggedInStatus)
    whenever(userSession.loggedInUser()).thenReturn(
        Observable.just(
            Just(user),
            Just(user.copy(loggedInStatus = curLoggedInStatus)),
            Just(user.copy(loggedInStatus = nextLoggedInStatus)))
    )
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.complete())
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.just(true))
    whenever(hasUserDismissedApprovedStatus.get()).thenReturn(true)
    whenever(approvalStatusApprovedAt.get()).thenReturn(Instant.now().minus(25, ChronoUnit.HOURS))

    uiEvents.onNext(ScreenCreated())

    if (shouldHideUserAccountStatus) {
      verify(screen, atLeastOnce()).hideUserAccountStatus()
    } else {
      verify(screen, never()).hideUserAccountStatus()
    }
  }

  @Test
  fun `when the user decides to enter the login code manually, the enter otp screen must be opened`() {
    uiEvents.onNext(PatientsEnterCodeManuallyClicked())
    verify(screen).openEnterCodeManuallyScreen()
  }

  @Test
  @Parameters(value = ["true", "false"])
  fun `the scan card button must be toggled based on the scan simple card feature flag`(scanCardFeatureEnabled: Boolean) {
    configEmitter.onNext(PatientConfig(limitOfSearchResults = 1, scanSimpleCardFeatureEnabled = scanCardFeatureEnabled, recentPatientLimit = 10))
    whenever(userSession.loggedInUser()).thenReturn(Observable.never())
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.never())

    uiEvents.onNext(ScreenCreated())

    verify(screen).setScanCardButtonEnabled(scanCardFeatureEnabled)
  }

  @Test
  fun `when the user clicks scan card id button, request for camera permissions`() {
    uiEvents.onNext(ScanCardIdButtonClicked)
    verify(screen).requestCameraPermissions()
  }

  @Test
  @Parameters(method = "params for opening scan card screen on camera permissions")
  fun `when the camera permissions are granted, the scan card screen must be opened`(
      permissionResult: RuntimePermissionResult,
      shouldOpenScreen: Boolean
  ) {
    uiEvents.onNext(PatientsScreenCameraPermissionChanged(permissionResult))
    if (shouldOpenScreen) {
      verify(screen).openScanSimpleIdCardScreen()
    } else {
      verify(screen, never()).openScanSimpleIdCardScreen()
    }
  }

  @Suppress("Unused")
  private fun `params for opening scan card screen on camera permissions`(): List<List<Any>> {
    return listOf(
        listOf(RuntimePermissionResult.GRANTED, true),
        listOf(RuntimePermissionResult.DENIED, false),
        listOf(RuntimePermissionResult.NEVER_ASK_AGAIN, false))
  }

  @Test
  fun `sync indicator should be visible only when user is approved for syncing`() {
    val user = PatientMocker.loggedInUser(status = WAITING_FOR_APPROVAL).toOptional()
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(user))
    whenever(userSession.refreshLoggedInUser()).thenReturn(Completable.never())
    whenever(hasUserDismissedApprovedStatus.asObservable()).thenReturn(Observable.just(false))
    whenever(hasUserDismissedApprovedStatus.get()).thenReturn(false)

    uiEvents.onNext(ScreenCreated())

    canSyncStream.onNext(false)
    canSyncStream.onNext(true)
    canSyncStream.onNext(true)

    val inOrder = inOrder(screen)
    inOrder.verify(screen).hideSyncIndicator()
    inOrder.verify(screen).showSyncIndicator()
  }
}
