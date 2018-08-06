package org.simple.clinic.registration.phone

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.registration.FindUserResult
import org.simple.clinic.registration.FindUserResult.Found
import org.simple.clinic.registration.FindUserResult.NetworkError
import org.simple.clinic.registration.FindUserResult.NotFound
import org.simple.clinic.registration.FindUserResult.UnexpectedError
import org.simple.clinic.user.OngoingLoginEntry
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
        showValidationError(replayedEvents),
        hideValidationError(replayedEvents),
        saveOngoingEntryAndProceed(replayedEvents))
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
              .toObservable() // Because Single.filter() returns a Maybe and Maybe.flatMapSingle() errors on completion.
              .filter { present -> present }
              .flatMapSingle { userSession.ongoingRegistrationEntry() }
              .map { { ui: Ui -> ui.preFillUserDetails(it) } }
        }

    return createEmptyEntry.mergeWith(preFill)
  }

  private fun showValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    val phoneNumberTextChanges = events
        .ofType<RegistrationPhoneNumberTextChanged>()
        .map { it.phoneNumber }

    return events
        .ofType<RegistrationPhoneDoneClicked>()
        .withLatestFrom(phoneNumberTextChanges)
        .map { (_, number) -> numberValidator.isValid(number) }
        .filter { isValidNumber -> isValidNumber.not() }
        .map { { ui: Ui -> ui.showInvalidNumberError() } }
  }

  private fun hideValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RegistrationPhoneNumberTextChanged>()
        .map { { ui: Ui -> ui.hideAnyError() } }
  }

  private fun saveOngoingEntryAndProceed(events: Observable<UiEvent>): Observable<UiChange> {
    val phoneNumberTextChanges = events.ofType<RegistrationPhoneNumberTextChanged>().map { it.phoneNumber }
    val doneClicks = events.ofType<RegistrationPhoneDoneClicked>()

    return doneClicks
        .withLatestFrom(phoneNumberTextChanges)
        .filter { (_, number) -> numberValidator.isValid(number) }
        .flatMap { (_, number) ->
          val cachedUserFindResult = userSession.findExistingUser(number)
              .cache()
              .toObservable()

          val showAndHideProgress = cachedUserFindResult
              .flatMap {
                when (it) {
                  is Found, is NotFound -> Observable.never()
                  is NetworkError -> Observable.just(
                      { ui: Ui -> ui.hideProgressIndicator() },
                      { ui: Ui -> ui.showNetworkErrorMessage() })
                  is UnexpectedError -> Observable.just(
                      { ui: Ui -> ui.hideProgressIndicator() },
                      { ui: Ui -> ui.showUnexpectedErrorMessage() })
                }
              }
              .startWith(Observable.just({ ui: Ui -> ui.hideAnyError() }, { ui: Ui -> ui.showProgressIndicator() }))

          val proceedToLogin = cachedUserFindResult
              .ofType<FindUserResult.Found>()
              .flatMap {
                userSession.saveOngoingLoginEntry(OngoingLoginEntry(phoneNumber = it.user.phoneNumber, otp = ""))
                    .andThen(userSession.clearOngoingRegistrationEntry())
                    .andThen(Observable.just({ ui: Ui -> ui.openLoginPinEntryScreen() }))
              }

          val proceedWithRegistration = cachedUserFindResult
              .ofType<FindUserResult.NotFound>()
              .flatMap {
                userSession.ongoingRegistrationEntry()
                    .map { it.copy(phoneNumber = number) }
                    .flatMapCompletable { userSession.saveOngoingRegistrationEntry(it) }
                    .andThen(Observable.just({ ui: Ui -> ui.openRegistrationNameEntryScreen() }))
              }

          Observable.merge(showAndHideProgress, proceedToLogin, proceedWithRegistration)
        }
  }
}
