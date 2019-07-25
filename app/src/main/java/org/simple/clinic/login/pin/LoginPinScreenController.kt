package org.simple.clinic.login.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.user.RequestLoginOtp
import org.simple.clinic.user.RequestLoginOtp.Result.NetworkError
import org.simple.clinic.user.RequestLoginOtp.Result.OtherError
import org.simple.clinic.user.RequestLoginOtp.Result.ServerError
import org.simple.clinic.user.RequestLoginOtp.Result.Success
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.UiEvent
import timber.log.Timber
import javax.inject.Inject

typealias Ui = LoginPinScreen
typealias UiChange = (Ui) -> Unit

class LoginPinScreenController @Inject constructor(
    private val userSession: UserSession,
    private val requestLoginOtp: RequestLoginOtp,
    val facilitySync: FacilitySync
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.merge(
        screenSetups(replayedEvents),
        submitClicks(replayedEvents),
        backClicks(replayedEvents),
        readPinDigestToVerify(replayedEvents))
  }

  private fun screenSetups(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinScreenCreated>()
        .flatMapSingle { _ ->
          userSession.ongoingLoginEntry()
              .map { { ui: Ui -> ui.showPhoneNumber(it.phoneNumber!!) } }
        }
  }

  private fun submitClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val enteredPin = events
        .ofType<LoginPinAuthenticated>()
        .map { it.pin }

    val updateLoginEntryWithPin = enteredPin
        .flatMapSingle { pin ->
          userSession
              .ongoingLoginEntry()
              .map { it.copy(pin = pin) }
        }
        .flatMapSingle { newEntry ->
          userSession
              .saveOngoingLoginEntry(newEntry)
              .toSingleDefault(newEntry)
        }

    return updateLoginEntryWithPin
        .flatMap { entry ->
          val syncFacilities = facilitySync
              .pullWithResult()
              .toObservable()
              .share()

          val requestOtpOnSuccessfulFacilitySync = syncFacilities
              .filter { it is FacilityPullResult.Success }
              .flatMapSingle { requestLoginOtp.requestForUser(entry.uuid) }
              .doOnNext { result ->
                if (result is OtherError) {
                  Timber.e(result.cause)
                }
              }

          val uiChangesForFailedFacilitySync: Observable<UiChange> = syncFacilities
              .filter { it !is FacilityPullResult.Success }
              .map { result ->
                { ui: Ui ->
                  when (result) {
                    is FacilityPullResult.NetworkError -> ui.showNetworkError()
                    is FacilityPullResult.UnexpectedError -> ui.showUnexpectedError()
                    is FacilityPullResult.Success -> throw RuntimeException("Success should not be handled here")
                  }.exhaustive()
                }
              }

          val uiChangesForRequestLoginOtp: Observable<UiChange> = requestOtpOnSuccessfulFacilitySync
              .map { result ->
                when (result) {
                  is Success -> { ui: Ui -> ui.openHomeScreen() }
                  is NetworkError -> { ui: Ui -> ui.showNetworkError() }
                  is ServerError, is OtherError -> { ui: Ui -> ui.showUnexpectedError() }
                }
              }

          Observable.merge(uiChangesForFailedFacilitySync, uiChangesForRequestLoginOtp)
        }
  }

  private fun backClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinBackClicked>()
        .flatMap {
          userSession.clearOngoingLoginEntry()
              .andThen(Observable.just({ ui: Ui -> ui.goBackToRegistrationScreen() }))
        }
  }

  private fun readPinDigestToVerify(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinScreenCreated>()
        .flatMapSingle { userSession.ongoingLoginEntry() }
        .map { it.pinDigest }
        .map { pinDigestToVerify -> { ui: Ui -> ui.submitWithPinDigest(pinDigestToVerify) } }
  }
}
