package org.simple.clinic.registration.phone.loggedout

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserSession.LogoutResult.Failure
import org.simple.clinic.user.UserSession.LogoutResult.Success
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LoggedOutOfDeviceDialog
typealias UiChange = (Ui) -> Unit

class LoggedOutOfDeviceDialogController @Inject constructor(
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return logoutUser(replayedEvents)
  }

  private fun logoutUser(events: Observable<UiEvent>): Observable<UiChange> {
    val logoutResultStream = events
        .ofType<ScreenCreated>()
        .flatMapSingle { userSession.logout() }
        .replay()
        .refCount()

    val enableOkayButton = logoutResultStream
        .ofType<Success>()
        .map { { ui: Ui -> ui.enableOkayButton() } }

    /*
    * This is an unlikely case since the logout process is completely
    * local and does not involve accessing network resources. However,
    * *if* it fails, it means something has really gone wrong and the
    * local persisted store might be in an unrecoverable state and we
    * cannot assume the logout process might have erased all private
    * patient information.
    *
    * In this event, it seems more prudent to let the app crash (and
    * forward this event to crash reporting) and let the user uninstall
    * and reinstall the app from a clean state.
    **/
    val forwardErrorOnFailure = logoutResultStream
        .ofType<Failure>()
        .flatMap { Observable.error<UiChange>(it.cause) }

    return enableOkayButton.mergeWith(forwardErrorOnFailure)
  }
}
