package org.simple.clinic.registration.phone

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
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
        updateOngoingEntryAndProceed(replayedEvents))
  }

  private fun createEmptyOngoingEntry(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationPhoneScreenCreated>()
        .flatMap {
          userSession
              .saveOngoingRegistrationEntry(OngoingRegistrationEntry(uuid = UUID.randomUUID()))
              .andThen(Observable.empty<UiChange>())
        }
  }

  private fun updateOngoingEntryAndProceed(events: Observable<UiEvent>): Observable<UiChange> {
    val phoneNumberTextChanges = events.ofType<RegistrationPhoneNumberTextChanged>()
    val doneClicks = events.ofType<RegistrationPhoneDoneClicked>()

    return doneClicks
        .withLatestFrom(phoneNumberTextChanges.map { it.phoneNumber })
        .flatMap { (_, phoneNumber) ->
          userSession.ongoingRegistrationEntry()
              .map { it.copy(phoneNumber = phoneNumber) }
              .flatMapCompletable { userSession.saveOngoingRegistrationEntry(it) }
              .andThen(Observable.just({ ui: Ui -> ui.openRegistrationNameEntryScreen() }))
        }
  }
}
