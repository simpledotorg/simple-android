package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.BackpressureStrategy
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.NOT_LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus.APPROVED_FOR_SYNCING
import org.simple.clinic.user.UserStatus.DISAPPROVED_FOR_SYNCING
import org.simple.clinic.user.UserStatus.WAITING_FOR_APPROVAL
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
    private val syncScheduler: SyncScheduler,
    @Named("approval_status_changed_at") private val approvalStatusUpdatedAtPref: Preference<Instant>,
    @Named("approved_status_dismissed") private val hasUserDismissedApprovedStatusPref: Preference<Boolean>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay(1).refCount()

    return Observable.mergeArray(
        enterCodeManuallyClicks(replayedEvents),
        newPatientClicks(replayedEvents),
        refreshApprovalStatusOnStart(replayedEvents),
        displayUserAccountStatusNotification(replayedEvents),
        dismissApprovalStatus(replayedEvents))
  }

  private fun enterCodeManuallyClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientsEnterCodeManuallyClicked>()
        .map { { ui: Ui -> ui.openEnterCodeManuallyScreen() } }
  }

  private fun newPatientClicks(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return events.ofType(NewPatientClicked::class.java)
        .map { { ui: PatientsScreen -> ui.openNewPatientScreen() } }
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
        .subscribeOn(Schedulers.io())
        .onErrorComplete()
        .doOnComplete { approvalStatusUpdatedAtPref.set(Instant.now()) }

    val syncData = userSession.loggedInUser()
        .take(1)
        .observeOn(io())
        .filter { (user) -> user?.isApprovedForSyncing() ?: false }
        .flatMapCompletable { syncScheduler.syncImmediately() }

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
              LOGGED_IN -> ui.hideUserAccountStatus()
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
}
