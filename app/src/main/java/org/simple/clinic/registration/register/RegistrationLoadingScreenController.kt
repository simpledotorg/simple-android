package org.simple.clinic.registration.register

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.registeruser.RegisterUser
import org.simple.clinic.user.registeruser.RegistrationResult.NetworkError
import org.simple.clinic.user.registeruser.RegistrationResult.Success
import org.simple.clinic.user.registeruser.RegistrationResult.UnexpectedError
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationLoadingUi
typealias UiChange = (Ui) -> Unit

class RegistrationLoadingScreenController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val registerUser: RegisterUser
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return registerOnStart(events)
  }

  private fun registerOnStart(events: Observable<UiEvent>): Observable<UiChange> {

    val retryClicks = events.ofType<RegisterErrorRetryClicked>()
    val creates = events.ofType<ScreenCreated>()

    val register = Observable
        .merge(creates, retryClicks)
        .flatMap {
          val savedUser = userSession
              .loggedInUser()
              .filterAndUnwrapJust()
              .take(1)

          val facilityToRegisterAt = savedUser
              .flatMap { facilityRepository.currentFacility(it) }
              .take(1)

          Observables.combineLatest(savedUser, facilityToRegisterAt)
              .flatMapSingle { (user, facility) -> registerUser.registerUserAtFacility(user, facility) }
        }
        .replay()
        .refCount()

    val clearOngoingEntry = register
        .filter { it is Success }
        .doOnNext { userSession.clearOngoingRegistrationEntry() }
        .flatMap { Observable.never<UiChange>() }

    val showScreenChanges = register.map {
      { ui: Ui ->
        when (it) {
          Success -> ui.openHomeScreen()
          NetworkError -> ui.showNetworkError()
          UnexpectedError -> ui.showUnexpectedError()
        }
      }
    }
    return showScreenChanges.mergeWith(clearOngoingEntry)
  }
}
