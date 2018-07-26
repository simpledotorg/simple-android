package org.simple.clinic.registration.name

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationFullNameScreen
typealias UiChange = (Ui) -> Unit

class RegistrationFullNameScreenController @Inject constructor(
    val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.merge(
        createEmptyOngoingEntry(replayedEvents),
        enableNextButton(replayedEvents),
        disableNextButton(replayedEvents),
        updateOngoingEntryAndProceed(replayedEvents))
  }

  private fun createEmptyOngoingEntry(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationFullNameScreenCreated>()
        .flatMap {
          userSession
              .saveOngoingRegistrationEntry(OngoingRegistrationEntry())
              .andThen(Observable.empty<UiChange>())
        }
  }

  private fun updateOngoingEntryAndProceed(events: Observable<UiEvent>): Observable<UiChange> {
    val fullNameTextChanges = events.ofType<RegistrationFullNameTextChanged>()
    val nextClicks = events.ofType<RegistrationFullNameNextClicked>()

    return nextClicks
        .withLatestFrom(fullNameTextChanges.map { it.fullName })
        .flatMap { (_, fullName) ->
          userSession.ongoingRegistrationEntry()
              .map { it.copy(fullName = fullName) }
              .flatMapCompletable { userSession.saveOngoingRegistrationEntry(it) }
              .andThen(Observable.just({ ui: Ui -> ui.openRegistrationNameEntryScreen() }))
        }
  }

  private fun enableNextButton(events: Observable<UiEvent>): Observable<UiChange> {
    return setNextButtonEnabled(events, true)
  }

  private fun disableNextButton(events: Observable<UiEvent>): Observable<UiChange> {
    return setNextButtonEnabled(events, false)
  }

  private fun setNextButtonEnabled(events: Observable<UiEvent>, enabled: Boolean): Observable<UiChange> {
    return events
        .ofType<RegistrationFullNameTextChanged>()
        .map { it.fullName.isBlank() }
        .distinctUntilChanged()
        .filter { isBlank -> isBlank != enabled }
        .map { { ui: Ui -> ui.setNextButtonEnabled(enabled) } }
  }
}
