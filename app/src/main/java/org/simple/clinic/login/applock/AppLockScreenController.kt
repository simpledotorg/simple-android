package org.simple.clinic.login.applock

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.login.applock.ComparisonResult.DIFFERENT
import org.simple.clinic.login.applock.ComparisonResult.SAME
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

typealias Ui = AppLockScreen
typealias UiChange = (Ui) -> Unit

class AppLockScreenController @Inject constructor(
    private val userSession: UserSession,
    private val passwordHasher: PasswordHasher,
    private val facilityRepository: FacilityRepository,
    @Named("should_lock_after") private val lockAfterTimestamp: Preference<Instant>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.mergeArray(
        populateFullName(replayedEvents),
        populateFacilityName(replayedEvents),
        resetValidationError(replayedEvents),
        validatePin(replayedEvents),
        exitOnBackClick(replayedEvents),
        openFacilityChangeScreen(replayedEvents),
        showConfirmResetPinDialog(replayedEvents)
    )
  }

  private fun populateFullName(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockScreenCreated>()
        .flatMap { _ ->
          userSession.loggedInUser()
              .filter { it is Just<User> }
              .map { (user) -> user!! }
              .map { it.fullName }
        }
        .map { { ui: Ui -> ui.setUserFullName(it) } }
  }

  private fun populateFacilityName(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockScreenCreated>()
        .flatMap { facilityRepository.currentFacility(userSession) }
        .map { { ui: Ui -> ui.setFacilityName(it.name) } }
  }

  private fun resetValidationError(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockScreenPinTextChanged>()
        .map { { ui: Ui -> ui.setIncorrectPinErrorVisible(false) } }
  }

  private fun validatePin(events: Observable<UiEvent>): Observable<UiChange> {
    val pinTextChanges = events
        .ofType<AppLockScreenPinTextChanged>()
        .map { it.pin }

    return events
        .ofType<AppLockScreenSubmitClicked>()
        .withLatestFrom(pinTextChanges)
        .flatMap { (_, enteredPin) ->
          val cachedPinValidation = userSession.loggedInUser()
              .map { (it as Just).value }
              .map { it.pinDigest }
              .firstOrError()
              .flatMap { pinDigest -> passwordHasher.compare(pinDigest, enteredPin) }

          val validationResultUiChange = cachedPinValidation
              .map {
                when (it) {
                  SAME -> { ui: Ui -> ui.restorePreviousScreen() }
                  DIFFERENT -> { ui: Ui ->
                    ui.setIncorrectPinErrorVisible(true)
                  }
                }
              }
              .toObservable()

          val progressUiChanges = cachedPinValidation
              .filter { it == DIFFERENT }
              .map { { ui: Ui -> ui.setProgressVisible(false) } }
              .toObservable()
              .startWith { ui: Ui -> ui.setProgressVisible(true) }

          val recordLastLock = cachedPinValidation
              .filter { it == SAME }
              .flatMapObservable {
                lockAfterTimestamp.delete()
                Observable.empty<UiChange>()
              }

          Observable.mergeArray(progressUiChanges, recordLastLock, validationResultUiChange)
        }
  }

  private fun exitOnBackClick(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockScreenBackClicked>()
        .map { { ui: Ui -> ui.exitApp() } }
  }

  private fun openFacilityChangeScreen(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockFacilityClicked>()
        .map { { ui: Ui -> ui.openFacilityChangeScreen() } }
  }

  private fun showConfirmResetPinDialog(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<AppLockForgotPinClicked>()
        .map { { ui: Ui -> ui.showConfirmResetPinDialog() } }
  }
}
