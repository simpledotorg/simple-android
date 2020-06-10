package org.simple.clinic.registration.name

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationNameUi
typealias UiChange = (Ui) -> Unit

class RegistrationFullNameScreenController @Inject constructor(
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.mergeArray(
        showValidationError(replayedEvents),
        hideValidationError(replayedEvents),
        updateOngoingEntryAndProceed(replayedEvents)
    )
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
        .map { (_, name) -> ongoingRegistrationEntry().copy(fullName = name) }
        .doOnNext(userSession::saveOngoingRegistrationEntry)
        .map { { ui: Ui -> ui.openRegistrationPinEntryScreen() } }
  }

  private fun ongoingRegistrationEntry(): OngoingRegistrationEntry = (userSession.ongoingRegistrationEntry() as Just).value
}
