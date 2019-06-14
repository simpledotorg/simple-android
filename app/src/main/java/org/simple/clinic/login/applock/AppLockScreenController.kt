package org.simple.clinic.login.applock

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

typealias Ui = AppLockScreen
typealias UiChange = (Ui) -> Unit

class AppLockScreenController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    @Named("should_lock_after") private val lockAfterTimestamp: Preference<Instant>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        populateFullName(replayedEvents),
        populateFacilityName(replayedEvents),
        unlockOnAuthentication(replayedEvents),
        exitOnBackClick(replayedEvents),
        showConfirmResetPinDialog(replayedEvents),
        readPinDigestToVerify(replayedEvents)
    )
  }

  private fun populateFullName(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockScreenCreated>()
        .flatMap { userSession.requireLoggedInUser() }
        .take(1)
        .map { loggedInUser -> loggedInUser.fullName }
        .map { { ui: Ui -> ui.setUserFullName(it) } }
  }

  private fun populateFacilityName(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockScreenCreated>()
        .flatMap { userSession.requireLoggedInUser() }
        .take(1)
        .switchMap { loggedInUser -> facilityRepository.currentFacility(loggedInUser) }
        .map { { ui: Ui -> ui.setFacilityName(it.name) } }
  }

  private fun unlockOnAuthentication(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockPinAuthenticated>()
        .doOnNext {
          // It is important that lockAfterTimestamp is cleared before
          // the app is unlocked. Otherwise, exiting the lockscreen will
          // dispose the controller and the timestamp will never get
          // cleared.
          lockAfterTimestamp.delete()
        }
        .map { { ui: Ui -> ui.restorePreviousScreen() } }
  }

  private fun exitOnBackClick(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockBackClicked>()
        .map { { ui: Ui -> ui.exitApp() } }
  }

  private fun showConfirmResetPinDialog(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockForgotPinClicked>()
        .map { { ui: Ui -> ui.showConfirmResetPinDialog() } }
  }

  private fun readPinDigestToVerify(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockScreenCreated>()
        .flatMap { userSession.requireLoggedInUser() }
        .take(1)
        .map { loggedInUser -> loggedInUser.pinDigest }
        .map { unlockWithPinDigest -> { ui: Ui -> ui.unlockWithPinDigest(unlockWithPinDigest) } }
  }
}
