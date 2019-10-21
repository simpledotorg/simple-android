package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.BackpressureStrategy
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.activity.TheActivityLifecycle.Resumed
import org.simple.clinic.appupdate.AppUpdateState.ShowAppUpdate
import org.simple.clinic.appupdate.CheckAppUpdateAvailability
import org.simple.clinic.illustration.HomescreenIllustrationRepository
import org.simple.clinic.patient.PatientConfig
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.NOT_LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus.ApprovedForSyncing
import org.simple.clinic.user.UserStatus.DisapprovedForSyncing
import org.simple.clinic.user.UserStatus.Unknown
import org.simple.clinic.user.UserStatus.WaitingForApproval
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Named

typealias Ui = PatientsScreen
typealias UiChange = (Ui) -> Unit

class PatientsScreenController @Inject constructor(
    private val userSession: UserSession,
    private val configProvider: Observable<PatientConfig>,
    private val checkAppUpdate: CheckAppUpdateAvailability,
    private val utcClock: UtcClock,
    private val userClock: UserClock,
    private val homescreenIllustrationRepository: HomescreenIllustrationRepository,
    @Named("approval_status_changed_at") private val approvalStatusUpdatedAtPref: Preference<Instant>,
    @Named("approved_status_dismissed") private val hasUserDismissedApprovedStatusPref: Preference<Boolean>,
    @Named("app_update_last_shown_at") private val appUpdateDialogShownAtPref: Preference<Instant>,
    @Named("number_of_patients_registered") private val numberOfPatientsRegisteredPref: Preference<Int>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        enterCodeManuallyClicks(replayedEvents),
        newPatientClicks(replayedEvents),
        refreshApprovalStatusOnStart(replayedEvents),
        displayUserAccountStatusNotification(replayedEvents),
        dismissApprovalStatus(replayedEvents),
        toggleVisibilityOfScanCardButton(replayedEvents),
        requestCameraPermissions(replayedEvents),
        openScanSimpleIdScreen(replayedEvents),
        toggleVisibilityOfSyncIndicator(replayedEvents),
        showAppUpdateDialog(replayedEvents),
        showSimpleVideo(replayedEvents),
        openSimpleVideo(replayedEvents),
        showIllustration(replayedEvents)
    )
  }

  private fun showIllustration(events: Observable<UiEvent>): Observable<UiChange> =
      screenCreated(events)
          .flatMap { homescreenIllustrationRepository.illustrationImageToShow() }
          .map { file -> { ui: Ui -> ui.showIllustration(file) } }

  private fun screenCreated(events: Observable<UiEvent>): Observable<ScreenCreated> = events.ofType()

  private fun enterCodeManuallyClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientsEnterCodeManuallyClicked>()
        .map { { ui: Ui -> ui.openEnterCodeManuallyScreen() } }
  }

  private fun newPatientClicks(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return events.ofType<NewPatientClicked>()
        .map { { ui: PatientsScreen -> ui.openPatientSearchScreen() } }
  }

  private fun refreshApprovalStatusOnStart(events: Observable<UiEvent>): Observable<UiChange> {
    val screenResumes = events.ofType<Resumed>()

    return Observable.merge(screenCreated(events), screenResumes)
        // Depending upon the entry point of Patients screen, both screen-create
        // and screen-resume events may or may not happen at the same time.
        // So duplicate triggers are dropped by restricting flatMap's
        // concurrency to 1.
        .toFlowable(BackpressureStrategy.LATEST)
        .flatMapMaybe({
          userSession.loggedInUser()
              .firstOrError()
              .filter { (user) -> user != null }
              .doOnSuccess { (user) ->
                val userStatus = user?.status
                // Resetting this flag here to show the approved status later
                if (userStatus != ApprovedForSyncing && hasUserDismissedApprovedStatusPref.get()) {
                  hasUserDismissedApprovedStatusPref.set(false)
                }

                // The refresh call should not get canceled when the app is closed
                // (i.e., this chain gets disposed). So it's not a part of this Rx chain.
                refreshUserStatus()
              }
              .flatMap { Maybe.empty<UiChange>() }
        }, false, 1)
        .toObservable()
  }

  private fun refreshUserStatus() {
    userSession.refreshLoggedInUser()
        .subscribeOn(io())
        .onErrorComplete()
        .doOnComplete { approvalStatusUpdatedAtPref.set(Instant.now()) }
        .subscribe()
  }

  private fun displayUserAccountStatusNotification(events: Observable<UiEvent>): Observable<UiChange> {
    return screenCreated(events)
        .flatMap {
          val user = userSession.loggedInUser().map { (user) -> user!! }

          val setVerificationStatusMessageVisible = { loggedInStatus: User.LoggedInStatus, ui: Ui ->
            when (loggedInStatus) {
              NOT_LOGGED_IN, OTP_REQUESTED -> ui.showUserStatusAsPendingVerification()
              LOGGED_IN, RESETTING_PIN -> ui.hideUserAccountStatus()
            }
          }

          Observables.combineLatest(user, hasUserDismissedApprovedStatusPref.asObservable())
              .map { (user, userDismissedStatus) ->
                when (user.status) {
                  WaitingForApproval -> { ui: Ui -> ui.showUserStatusAsWaiting() }
                  DisapprovedForSyncing -> { ui: Ui -> setVerificationStatusMessageVisible(user.loggedInStatus, ui) }
                  ApprovedForSyncing -> {
                    val twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS)
                    val wasApprovedInLast24Hours = twentyFourHoursAgo < approvalStatusUpdatedAtPref.get()

                    if (userDismissedStatus.not() && wasApprovedInLast24Hours) {
                      { ui: Ui -> ui.showUserStatusAsApproved() }
                    } else {
                      { ui: Ui -> setVerificationStatusMessageVisible(user.loggedInStatus, ui) }
                    }
                  }
                  is Unknown -> { _ -> }
                }
              }
        }
  }

  private fun dismissApprovalStatus(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<UserApprovedStatusDismissed>()
        .flatMap {
          hasUserDismissedApprovedStatusPref.set(true)
          Observable.never<UiChange>()
        }
  }

  private fun toggleVisibilityOfScanCardButton(events: Observable<UiEvent>): Observable<UiChange> {
    val isScanCardFeatureEnabledStream = configProvider
        .map { it.scanSimpleCardFeatureEnabled }

    return screenCreated(events)
        .withLatestFrom(isScanCardFeatureEnabledStream)
        .map { (_, isScanCardFeatureEnabled) ->
          { ui: Ui -> ui.setScanCardButtonEnabled(isScanCardFeatureEnabled) }
        }
  }

  private fun requestCameraPermissions(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScanCardIdButtonClicked>()
        .map { Ui::requestCameraPermissions }
  }

  private fun openScanSimpleIdScreen(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientsScreenCameraPermissionChanged>()
        .filter { it.permissionResult == RuntimePermissionResult.GRANTED }
        .map { Ui::openScanSimpleIdCardScreen }
  }

  private fun toggleVisibilityOfSyncIndicator(events: Observable<UiEvent>): Observable<UiChange> {
    val canUserSync =
        userSession
            .canSyncData()
            .distinctUntilChanged()

    return Observables
        .combineLatest(screenCreated(events), canUserSync)
        .map { (_, canSync) ->
          { ui: Ui ->
            when {
              canSync -> ui.showSyncIndicator()
              else -> ui.hideSyncIndicator()
            }
          }
        }
  }

  private fun showAppUpdateDialog(events: Observable<UiEvent>): Observable<UiChange> {

    fun hasADayPassedSinceLastUpdateShown(): Boolean {
      val today = LocalDate.now(userClock)
      val lastShownDate = appUpdateDialogShownAtPref.get().toLocalDateAtZone(userClock.zone)
      return lastShownDate.isBefore(today)
    }

    val availableUpdate = checkAppUpdate
        .listen()
        .filter { it is ShowAppUpdate }


    return Observables
        .combineLatest(screenCreated(events), availableUpdate)
        .map { (_, update) -> update }
        .filter { hasADayPassedSinceLastUpdateShown() }
        .doOnNext { appUpdateDialogShownAtPref.set(Instant.now(utcClock)) }
        .map { Ui::showAppUpdateDialog }
  }

  private fun showSimpleVideo(events: Observable<UiEvent>): Observable<UiChange> {
    return screenCreated(events)
        .map {
          if (numberOfPatientsRegisteredPref.get() < 10) {
            { ui: Ui -> ui.showSimpleVideo() }
          } else {
            { ui: Ui -> ui.showIllustration() }
          }
        }
  }

  private fun openSimpleVideo(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SimpleVideoClicked>()
        .map { Ui::openYouTubeLinkForSimpleVideo }
  }
}
