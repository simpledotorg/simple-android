package org.simple.clinic.enterotp

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = EnterOtpScreen
typealias UiChange = (Ui) -> Unit

class EnterOtpScreenController @Inject constructor(
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return showPhoneNumberOnStart(replayedEvents)
  }

  private fun showPhoneNumberOnStart(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<EnterOtpScreenCreated>()
        .flatMapSingle { userSession.requireLoggedInUser().firstOrError() }
        .map { user -> { ui: Ui -> ui.showUserPhoneNumber(user.phoneNumber) } }
  }
}
