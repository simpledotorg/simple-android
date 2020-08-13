package org.simple.clinic.login.applock

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named

typealias Ui = AppLockScreenUi
typealias UiChange = (Ui) -> Unit

class AppLockScreenController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    @Named("should_lock_after") private val lockAfterTimestamp: Preference<Instant>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.mergeArray(
        populateFullName(replayedEvents),
        populateFacilityName(replayedEvents)
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
}
