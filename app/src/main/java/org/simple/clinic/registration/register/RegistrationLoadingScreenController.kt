package org.simple.clinic.registration.register

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.registration.RegistrationResult
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationLoadingScreen
typealias UiChange = (Ui) -> Unit

class RegistrationLoadingScreenController @Inject constructor(
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .flatMapSingle { userSession.register() }
        .map {
          when (it) {
            is RegistrationResult.Success -> { ui: Ui -> ui.openHomeScreen() }
            is RegistrationResult.Error -> { ui: Ui -> ui.showError() }
          }
        }
  }
}
