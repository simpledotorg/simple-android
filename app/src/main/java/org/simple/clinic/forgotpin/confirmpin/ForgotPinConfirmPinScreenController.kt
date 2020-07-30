package org.simple.clinic.forgotpin.confirmpin

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.clearpatientdata.SyncAndClearPatientData
import org.simple.clinic.user.resetpin.ResetPinResult.NetworkError
import org.simple.clinic.user.resetpin.ResetPinResult.Success
import org.simple.clinic.user.resetpin.ResetPinResult.UnexpectedError
import org.simple.clinic.user.resetpin.ResetPinResult.UserNotFound
import org.simple.clinic.user.resetpin.ResetUserPin
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

typealias Ui = ForgotPinConfirmPinUi
typealias UiChange = (Ui) -> Unit

class ForgotPinConfirmPinScreenController @AssistedInject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val resetUserPin: ResetUserPin,
    private val syncAndClearPatientData: SyncAndClearPatientData,
    @Assisted private val previousPin: String
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(previousPin: String): ForgotPinConfirmPinScreenController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.mergeArray(
        showFacilityOnScreenCreated(replayedEvents),
        hideErrorsOnPinTextChanged(replayedEvents),
        showMismatchedPinErrors(replayedEvents),
        showProgress(replayedEvents),
        syncPatientDataAndResetPin(replayedEvents)
    )
  }

  private fun showFacilityOnScreenCreated(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ScreenCreated>()
        .flatMap { userSession.loggedInUser() }
        .filterAndUnwrapJust()
        .switchMap { facilityRepository.currentFacility(it) }
        .map { facility -> { ui: Ui -> ui.showFacility(facility.name) } }
  }

  private fun hideErrorsOnPinTextChanged(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ForgotPinConfirmPinTextChanged>()
        .map { { ui: Ui -> ui.hideError() } }
  }

  private fun showMismatchedPinErrors(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ForgotPinConfirmPinSubmitClicked>()
        .map { it.pin }
        .filter { enteredPin -> enteredPin != previousPin }
        .map { { ui: Ui -> ui.showPinMismatchedError() } }
  }

  private fun showProgress(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ForgotPinConfirmPinSubmitClicked>()
        .map { it.pin }
        .filter { enteredPin -> enteredPin == previousPin }
        .map { { ui: Ui -> ui.showProgress() } }
  }

  private fun syncPatientDataAndResetPin(events: Observable<UiEvent>): Observable<UiChange> {
    val validPin = events.ofType<ForgotPinConfirmPinSubmitClicked>()
        .map { it.pin }
        .filter { enteredPin -> enteredPin == previousPin }
        .share()

    val makeResetPinCall = validPin
        .flatMapSingle { newPin -> syncAndClearPatientData.run().toSingleDefault(newPin) }
        .flatMapSingle { newPin -> setUserLoggedInStatusToResettingPin().toSingleDefault(newPin) }
        .flatMapSingle(resetUserPin::resetPin)
        .onErrorReturn(::UnexpectedError)
        .share()

    val showErrorsOnResetPinCall = makeResetPinCall
        .filter { it !is Success }
        .map { result ->
          when (result) {
            is NetworkError -> { ui: Ui -> ui.showNetworkError() }
            is UserNotFound, is UnexpectedError -> { ui: Ui -> ui.showUnexpectedError() }
            is Success -> { _: Ui -> }
          }
        }

    val openHomeOnResetPinCallSuccess = makeResetPinCall
        .filter { it is Success }
        .map { { ui: Ui -> ui.goToHomeScreen() } }

    return Observable.mergeArray(
        showErrorsOnResetPinCall,
        openHomeOnResetPinCallSuccess
    )
  }

  private fun setUserLoggedInStatusToResettingPin(): Completable {
    return userSession
        .loggedInUser()
        .filterAndUnwrapJust()
        .firstOrError()
        .flatMapCompletable { user -> userSession.updateLoggedInStatusForUser(user.uuid, RESETTING_PIN) }
  }
}
