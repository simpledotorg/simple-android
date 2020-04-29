package org.simple.clinic.login.pin

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LoginPinScreen
typealias UiChange = (Ui) -> Unit

class LoginPinScreenController @Inject constructor(
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(UpdateLoginEntryWithEnteredPin(userSession))
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        screenSetups(replayedEvents),
        backClicks(replayedEvents),
        loginUser(replayedEvents)
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
        loggedInStatus = User.LoggedInStatus.OTP_REQUESTED,
        registrationFacilityUuid = entry.registrationFacilityUuid!!,
        currentFacilityUuid = entry.registrationFacilityUuid
    )
  }

  private fun backClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PinBackClicked>()
        .doOnNext { userSession.clearOngoingLoginEntry() }
        .flatMap { Observable.just(Ui::goBackToRegistrationScreen) }
  }

  private fun loginUser(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<LoginPinScreenUpdatedLoginEntry>()
        .map { it.ongoingLoginEntry }
        .flatMap { entry ->
          createUserLocally(entry)
              .andThen(Observable.just { ui: Ui -> ui.openHomeScreen() })
        }
  }

  private fun createUserLocally(entry: OngoingLoginEntry): Completable {
    return userSession.storeUser(
        user = createUserFromLoginEntry(entry),
        facilityUuid = entry.registrationFacilityUuid!!
    )
  }
}
