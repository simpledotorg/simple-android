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
import org.simple.clinic.overdue.Appointment.Status.SCHEDULED
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientConfig
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSummaryResult
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.NOT_LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus.APPROVED_FOR_SYNCING
import org.simple.clinic.user.UserStatus.DISAPPROVED_FOR_SYNCING
import org.simple.clinic.user.UserStatus.WAITING_FOR_APPROVAL
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.util.unwrapJust
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Named

typealias Ui = PatientsScreen
typealias UiChange = (Ui) -> Unit

class PatientsScreenController @Inject constructor(
    private val userSession: UserSession,
    private val dataSync: DataSync,
    private val patientRepository: PatientRepository,
    private val appointmentRepository: AppointmentRepository,
    private val configProvider: Observable<PatientConfig>,
    @Named("approval_status_changed_at") private val approvalStatusUpdatedAtPref: Preference<Instant>,
    @Named("approved_status_dismissed") private val hasUserDismissedApprovedStatusPref: Preference<Boolean>,
    @Named("patient_summary_result") private val patientSummaryResult: Preference<PatientSummaryResult>
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
        showSummarySavedNotification(replayedEvents),
        toggleVisibilityOfScanCardButton(replayedEvents),
        requestCameraPermissions(replayedEvents),
        openScanSimpleIdScreen(replayedEvents),
        openSearchResultsScreenOnScannedPassport(replayedEvents))
  }

  private fun enterCodeManuallyClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientsEnterCodeManuallyClicked>()
        .map { { ui: Ui -> ui.openEnterCodeManuallyScreen() } }
  }

  private fun newPatientClicks(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return events.ofType<NewPatientClicked>()
        .map { { ui: PatientsScreen -> ui.openPatientSearchScreen() } }
  }

  private fun refreshApprovalStatusOnStart(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreate = events.ofType<ScreenCreated>()
    val screenResumes = events.ofType<TheActivityLifecycle.Resumed>()

    return Observable.merge(screenCreate, screenResumes)
        // Depending upon the entry point of Patients screen, both screen-create
        // and screen-resume events may or may not happen at the same time.
        // So duplicate triggers are dropped by restricting flatMap's
        // concurrency to 1.
        .toFlowable(BackpressureStrategy.LATEST)
        .flatMapMaybe({ _ ->
          userSession.loggedInUser()
              .firstOrError()
              .filter { (user) -> user!!.status == WAITING_FOR_APPROVAL }
              .doOnSuccess {
                // Resetting this flag here to show the approved status later
                if (hasUserDismissedApprovedStatusPref.get()) {
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
    val refreshUser = userSession.refreshLoggedInUser()
        .subscribeOn(io())
        .onErrorComplete()
        .doOnComplete { approvalStatusUpdatedAtPref.set(Instant.now()) }

    val syncData = userSession.canSyncData()
        .take(1)
        .observeOn(io())
        .filter { canSyncData -> canSyncData }
        .flatMapCompletable { dataSync.sync(null) }

    refreshUser
        .andThen(syncData)
        .subscribe()
  }

  private fun displayUserAccountStatusNotification(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
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
                  WAITING_FOR_APPROVAL -> { ui: Ui -> ui.showUserStatusAsWaiting() }
                  DISAPPROVED_FOR_SYNCING -> { ui: Ui -> setVerificationStatusMessageVisible(user.loggedInStatus, ui) }
                  APPROVED_FOR_SYNCING -> {
                    val twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS)
                    val wasApprovedInLast24Hours = twentyFourHoursAgo < approvalStatusUpdatedAtPref.get()

                    if (userDismissedStatus.not() && wasApprovedInLast24Hours) {
                      { ui: Ui -> ui.showUserStatusAsApproved() }
                    } else {
                      { ui: Ui -> setVerificationStatusMessageVisible(user.loggedInStatus, ui) }
                    }
                  }
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

  private fun showSummarySavedNotification(events: Observable<UiEvent>): Observable<UiChange> {
    val result = events
        .ofType<ScreenCreated>()
        .map { patientSummaryResult.get() }

    val savedStream = result
        .filter { it is PatientSummaryResult.Saved }
        .map { it as PatientSummaryResult.Saved }
        .flatMap { patientRepository.patient(it.patientUuid).take(1).unwrapJust() }
        .doOnNext { patientSummaryResult.delete() }
        .map { { ui: Ui -> ui.showStatusPatientSummarySaved(it.fullName) } }

    val scheduledResult = result
        .filter { it is PatientSummaryResult.Scheduled }
        .map { it as PatientSummaryResult.Scheduled }

    val patientNameFromScheduled = scheduledResult
        .flatMap { patientRepository.patient(it.patientUuid).take(1).unwrapJust() }
        .map { it.fullName }

    val appointmentDate = scheduledResult
        .flatMap { appointmentRepository.lastCreatedAppointmentForPatient(it.patientUuid) }
        .unwrapJust()
        .doOnNext { assert(it.status == SCHEDULED) { "Last appointment's status != 'scheduled'" } }
        .map { it.scheduledDate }

    val scheduledStream = Observables
        .zip(patientNameFromScheduled, appointmentDate)
        .doOnNext { patientSummaryResult.delete() }
        .map { (name, appointmentDate) ->
          { ui: Ui -> ui.showStatusPatientAppointmentSaved(name, appointmentDate) }
        }

    return Observable.merge(savedStream, scheduledStream)
  }

  private fun toggleVisibilityOfScanCardButton(events: Observable<UiEvent>): Observable<UiChange> {
    val isScanCardFeatureEnabledStream = configProvider
        .map { it.scanSimpleCardFeatureEnabled }

    return events
        .ofType<ScreenCreated>()
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

  private fun openSearchResultsScreenOnScannedPassport(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientsScreenBpPassportCodeScanned>()
        .map { Ui::openPatientSearchScreen }
  }
}
