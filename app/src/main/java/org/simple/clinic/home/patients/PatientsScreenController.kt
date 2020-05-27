package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.appupdate.AppUpdateState.ShowAppUpdate
import org.simple.clinic.appupdate.CheckAppUpdateAvailability
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.refreshuser.RefreshCurrentUser
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import javax.inject.Inject
import javax.inject.Named

typealias Ui = PatientsUi
typealias UiChange = (Ui) -> Unit

class PatientsScreenController @Inject constructor(
    private val userSession: UserSession,
    private val checkAppUpdate: CheckAppUpdateAvailability,
    private val utcClock: UtcClock,
    private val userClock: UserClock,
    private val refreshCurrentUser: RefreshCurrentUser,
    private val schedulersProvider: SchedulersProvider,
    @Named("approval_status_changed_at") private val approvalStatusUpdatedAtPref: Preference<Instant>,
    @Named("approved_status_dismissed") private val hasUserDismissedApprovedStatusPref: Preference<Boolean>,
    @Named("app_update_last_shown_at") private val appUpdateDialogShownAtPref: Preference<Instant>,
    @Named("number_of_patients_registered") private val numberOfPatientsRegisteredPref: Preference<Int>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.mergeArray(
        openScanSimpleIdScreen(replayedEvents),
        toggleVisibilityOfSyncIndicator(replayedEvents),
        showAppUpdateDialog(replayedEvents),
        showSimpleVideo(replayedEvents),
        openSimpleVideo(replayedEvents)
    )
  }

  private fun screenCreated(events: Observable<UiEvent>): Observable<ScreenCreated> = events.ofType()

  private fun openScanSimpleIdScreen(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScanCardIdButtonClicked>()
        .filter(ScanCardIdButtonClicked::isPermissionGranted)
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
