package org.simple.clinic.registration.name

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationFullNameScreen
typealias UiChange = (Ui) -> Unit

class RegistrationFullNameScreenController @Inject constructor(
    val userSession: UserSession,
    val facilityRepository: FacilityRepository,
    val facilitySync: FacilitySync
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        preFillExistingDetails(replayedEvents),
        pullFacilitiesInAdvance(replayedEvents),
        showValidationError(replayedEvents),
        hideValidationError(replayedEvents),
        updateOngoingEntryAndProceed(replayedEvents))
  }

  private fun preFillExistingDetails(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationFullNameScreenCreated>()
        .flatMapSingle {
          userSession.ongoingRegistrationEntry()
              .map { { ui: Ui -> ui.preFillUserDetails(it) } }
        }
  }

  private fun pullFacilitiesInAdvance(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationFullNameScreenCreated>()
        .flatMap {
          facilityRepository.recordCount()
              .take(1)
              .filter { count -> count == 0 }
              .flatMapCompletable { facilitySync.sync().onErrorComplete() }
              .andThen(Observable.empty<UiChange>())
        }
  }

  private fun showValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    val fullNameTextChanges = events.ofType<RegistrationFullNameTextChanged>().map { it.fullName }

    return events
        .ofType<RegistrationFullNameDoneClicked>()
        .withLatestFrom(fullNameTextChanges)
        .filter { (_, name) -> name.isBlank() }
        .map { { ui: Ui -> ui.showEmptyNameValidationError() } }
  }

  private fun hideValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationFullNameTextChanged>()
        .map { { ui: Ui -> ui.hideValidationError() } }
  }

  private fun updateOngoingEntryAndProceed(events: Observable<UiEvent>): Observable<UiChange> {
    val fullNameTextChanges = events.ofType<RegistrationFullNameTextChanged>().map { it.fullName }
    val doneClicks = events.ofType<RegistrationFullNameDoneClicked>()

    return doneClicks
        .withLatestFrom(fullNameTextChanges)
        .filter { (_, name) -> name.isNotBlank() }
        .flatMap { (_, name) ->
          userSession.ongoingRegistrationEntry()
              .map { it.copy(fullName = name) }
              .flatMapCompletable { userSession.saveOngoingRegistrationEntry(it) }
              .andThen(Observable.just({ ui: Ui -> ui.openRegistrationPinEntryScreen() }))
        }
  }
}
