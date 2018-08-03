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
    private val userSession: UserSession,
    private val numberValidator: PhoneNumberValidator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.merge(
        createEmptyOngoingEntryAndPreFill(replayedEvents),
        validateInputAndProceed(replayedEvents),
        resetValidationError(replayedEvents))
  }

  private fun createEmptyOngoingEntryAndPreFill(events: Observable<UiEvent>): Observable<UiChange> {
    val createEmptyEntry = events
        .ofType<RegistrationPhoneScreenCreated>()
        .flatMap {
          userSession.isOngoingRegistrationEntryPresent()
              .filter { present -> present.not() }
              .flatMapCompletable {
                userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(uuid = UUID.randomUUID()))
              }
              .andThen(Observable.empty<UiChange>())
        }

    val preFill = events
        .ofType<RegistrationPhoneScreenCreated>()
        .flatMap {
          userSession.isOngoingRegistrationEntryPresent()
              // Because Single.filter() returns a Maybe and
              // Maybe.flatMapSingle() errors on completion.
              .toObservable()
              .filter { present -> present }
              .flatMapSingle { userSession.ongoingRegistrationEntry() }
              .map { { ui: Ui -> ui.preFillUserDetails(it) } }
        }

    return createEmptyEntry.mergeWith(preFill)
  }

  private fun validateInputAndProceed(events: Observable<UiEvent>): Observable<UiChange> {
    val phoneNumberTextChanges = events.ofType<RegistrationPhoneNumberTextChanged>().map { it.phoneNumber }
    val doneClicks = events.ofType<RegistrationPhoneDoneClicked>()

    val proceeds = doneClicks
        .withLatestFrom(phoneNumberTextChanges)
        .filter { (_, number) -> numberValidator.isValid(number) }
        .take(1)
        .flatMap { (_, number) ->
          userSession.ongoingRegistrationEntry()
              .map { it.copy(phoneNumber = number) }
              .flatMapCompletable { userSession.saveOngoingRegistrationEntry(it) }
              .andThen(Observable.just({ ui: Ui -> ui.openRegistrationNameEntryScreen() }))
        }

    val validations = doneClicks
        .withLatestFrom(phoneNumberTextChanges)
        .map { (_, number) -> numberValidator.isValid(number) }
        .filter { isValidNumber -> isValidNumber.not() }
        .map { { ui: Ui -> ui.showInvalidNumberError() } }

    return validations.mergeWith(proceeds)
  }

  private fun resetValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationPhoneNumberTextChanged>()
        .map { { ui: Ui -> ui.hideInvalidNumberError() } }
  }
}
