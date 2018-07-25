package org.simple.clinic.registration.phone

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationPhoneScreen
typealias UiChange = (Ui) -> Unit

class RegistrationPhoneScreenController @Inject constructor(
    val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.merge(
        createEmptyOngoingEntry(replayedEvents),
        enableNextButton(replayedEvents),
        disableNextButton(replayedEvents),
        createOngoingEntryAndProceed(replayedEvents))
  }

  private fun createEmptyOngoingEntry(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationPhoneScreenCreated>()
        .flatMap {
          userSession
              .saveOngoingRegistrationEntry(OngoingRegistrationEntry())
              .andThen(Observable.empty<UiChange>())
        }
  }

  private fun createOngoingEntryAndProceed(events: Observable<UiEvent>): Observable<UiChange> {
    val phoneNumberTextChanges = events.ofType<RegistrationPhoneNumberTextChanged>()
    val nextClicks = events.ofType<RegistrationPhoneNextClicked>()

    return nextClicks
        .withLatestFrom(phoneNumberTextChanges.map { it.phoneNumber })
        .flatMap { (_, phoneNumber) ->
          userSession.ongoingRegistrationEntry()
              .map { it.copy(phoneNumber = phoneNumber) }
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
        .ofType<RegistrationPhoneNumberTextChanged>()
        .map { it.phoneNumber.isBlank() }
        .distinctUntilChanged()
        .filter { isBlank -> isBlank != enabled }
        .map { { ui: Ui -> ui.setNextButtonEnabled(enabled) } }
  }
}
