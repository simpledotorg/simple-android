package org.simple.clinic.login.pin

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.RequestLoginOtp
import org.simple.clinic.user.RequestLoginOtp.Result.NetworkError
import org.simple.clinic.user.RequestLoginOtp.Result.OtherError
import org.simple.clinic.user.RequestLoginOtp.Result.ServerError
import org.simple.clinic.user.RequestLoginOtp.Result.Success
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LoginPinScreen
typealias UiChange = (Ui) -> Unit

class LoginPinScreenController @Inject constructor(
    private val userSession: UserSession,
    private val requestLoginOtp: RequestLoginOtp,
    private val facilitySync: FacilitySync
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(UpdateLoginEntryWithEnteredPin(userSession))
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        screenSetups(replayedEvents),
        backClicks(replayedEvents),
        readPinDigestToVerify(replayedEvents),
        syncFacilitiesAndLoginUser(replayedEvents)
    )
  }

  private fun screenSetups(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinScreenCreated>()
        .flatMapSingle { _ ->
          userSession.ongoingLoginEntry()
              .map { { ui: Ui -> ui.showPhoneNumber(it.phoneNumber!!) } }
        }
  }

  private fun createUserFromLoginEntry(entry: OngoingLoginEntry): User {
    return User(
        uuid = entry.uuid,
        fullName = entry.fullName!!,
        phoneNumber = entry.phoneNumber!!,
        pinDigest = entry.pinDigest!!,
        status = entry.status!!,
        createdAt = entry.createdAt!!,
        updatedAt = entry.updatedAt!!,
        loggedInStatus = User.LoggedInStatus.OTP_REQUESTED
    )
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

  private fun syncFacilitiesAndLoginUser(events: Observable<UiEvent>): Observable<UiChange> {
    val updatedEntryStream = events
        .ofType<LoginPinScreenUpdatedLoginEntry>()
        .map { it.ongoingLoginEntry }

    val syncFacilityResults = updatedEntryStream
        .flatMapSingle { facilitySync.pullWithResult() }
        .share()

    val requestOtpResults = syncFacilityResults
        .ofType<FacilityPullResult.Success>()
        .withLatestFrom(updatedEntryStream) { _, entry -> entry.uuid }
        .flatMapSingle(requestLoginOtp::requestForUser)
        .share()

    val saveUserAndGoToHomeScreen = requestOtpResults
        .ofType<Success>()
        .withLatestFrom(updatedEntryStream) { _, entry -> entry }
        .flatMap { entry ->
          createUserLocally(entry)
              .andThen(Observable.just { ui: Ui -> ui.openHomeScreen() })
        }

    return Observable.merge(
        saveUserAndGoToHomeScreen,
        showErrorsForFacilityPullFailures(syncFacilityResults),
        showErrorsForRequestLoginOtpFailures(requestOtpResults)
    )
  }

  private fun createUserLocally(entry: OngoingLoginEntry): Completable {
    return userSession.storeUser(
        user = createUserFromLoginEntry(entry),
        facilityUuid = entry.registrationFacilityUuid!!
    )
  }

  private fun showErrorsForRequestLoginOtpFailures(requestOtpResults: Observable<RequestLoginOtp.Result>): Observable<(Ui) -> Unit> {
    return requestOtpResults
        .filter { it !is Success }
        .map { result ->
          { ui: Ui ->
            when (result) {
              NetworkError -> ui.showNetworkError()
              is ServerError, is OtherError -> ui.showUnexpectedError()
              Success -> throw RuntimeException("Success should not be handled here")
            }.exhaustive()
          }
        }
  }

  private fun showErrorsForFacilityPullFailures(syncFacilityResults: Observable<FacilityPullResult>): Observable<UiChange> {
    return syncFacilityResults
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
  }
}
