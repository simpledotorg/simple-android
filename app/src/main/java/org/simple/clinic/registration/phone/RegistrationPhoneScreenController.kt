package org.simple.clinic.registration.phone

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.registration.FindUserResult.Found
import org.simple.clinic.registration.FindUserResult.NetworkError
import org.simple.clinic.registration.FindUserResult.NotFound
import org.simple.clinic.registration.FindUserResult.UnexpectedError
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.VALID
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.MOBILE
import org.simple.clinic.user.LoggedInUserPayload
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
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

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
        .flatMap { _ ->
          userSession.isOngoingRegistrationEntryPresent()
              .toObservable() // Because Single.filter() returns a Maybe and Maybe.flatMapSingle() errors on completion.
              .filter { present -> present }
              .flatMapSingle { userSession.ongoingRegistrationEntry() }
              .map { { ui: Ui -> ui.preFillUserDetails(it) } }
        }

    return Observable.merge(createEmptyEntry, preFill)
  }

  private fun showValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    val phoneNumberTextChanges = events
        .ofType<RegistrationPhoneNumberTextChanged>()
        .map { it.phoneNumber }

    return events.ofType<RegistrationPhoneDoneClicked>()
        .withLatestFrom(phoneNumberTextChanges)
        .map { (_, number) -> numberValidator.validate(number, MOBILE) }
        .filter { it != VALID }
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
        .filter { (_, number) -> numberValidator.validate(number, MOBILE) == VALID }
        .flatMap { (_, number) ->
          val cachedUserFindResult = userSession.findExistingUser(number)
              .cache()
              .toObservable()

          val uiChangesForFindUser = cachedUserFindResult
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
              .ofType<Found>()
              .flatMap(this::saveFoundUserLocallyAndProceedToLogin)

          val proceedWithRegistration = cachedUserFindResult
              .ofType<NotFound>()
              .flatMap { _ ->
                userSession.ongoingRegistrationEntry()
                    .map { it.copy(phoneNumber = number) }
                    .flatMapCompletable { userSession.saveOngoingRegistrationEntry(it) }
                    .andThen(Observable.just({ ui: Ui -> ui.openRegistrationNameEntryScreen() }))
              }

          Observable.merge(uiChangesForFindUser, proceedToLogin, proceedWithRegistration)
        }
  }

  private fun saveFoundUserLocallyAndProceedToLogin(foundUser: Found): Observable<UiChange> {
    return Single.just(foundUser.user)
        .map(this::loggedInUserPayloadToOngoingLoginEntry)
        .flatMapCompletable(userSession::saveOngoingLoginEntry)
        .andThen(userSession.clearOngoingRegistrationEntry())
        .andThen(Observable.just { ui: Ui ->
          ui.hideProgressIndicator()
          ui.openLoginPinEntryScreen()
        })
        .onErrorReturn {
          { ui: Ui ->
            ui.hideProgressIndicator()
            ui.showUnexpectedErrorMessage()
          }
        }
  }

  private fun loggedInUserPayloadToOngoingLoginEntry(loggedInUserPayload: LoggedInUserPayload) = OngoingLoginEntry(
      uuid = loggedInUserPayload.uuid,
      phoneNumber = loggedInUserPayload.phoneNumber,
      pin = null,
      fullName = loggedInUserPayload.fullName,
      pinDigest = loggedInUserPayload.pinDigest,
      registrationFacilityUuid = loggedInUserPayload.registrationFacilityId,
      status = loggedInUserPayload.status,
      createdAt = loggedInUserPayload.createdAt,
      updatedAt = loggedInUserPayload.updatedAt
  )
}
