package org.simple.clinic.enterotp

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.login.LoginUserWithOtp
import org.simple.clinic.login.activateuser.ActivateUser
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.NewlyVerifiedUser
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.OngoingLoginEntryRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

typealias Ui = EnterOtpUi
typealias UiChange = (Ui) -> Unit

class EnterOtpScreenController @Inject constructor(
    private val userSession: UserSession,
    private val activateUser: ActivateUser,
    private val loginUserWithOtp: LoginUserWithOtp,
    private val ongoingLoginEntryRepository: OngoingLoginEntryRepository,
    private val schedulersProvider: SchedulersProvider,
    private val dataSync: DataSync
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.mergeArray(
        closeScreenOnUserLoginInBackground(replayedEvents),
        resendSms(replayedEvents)
    )
  }

  private fun closeScreenOnUserLoginInBackground(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ScreenCreated>()
        .flatMap { userSession.loggedInUser() }
        .compose(NewlyVerifiedUser())
        .map { { ui: Ui -> ui.goBack() } }
  }

  private fun resendSms(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<EnterOtpResendSmsClicked>()
        .map { loggedInUserUuid() }
        .flatMap(this::requestLoginOtpForUser)
  }

  private fun requestLoginOtpForUser(userUuid: UUID): Observable<UiChange> {
    val showProgressBeforeRequestingOtp = { ui: Ui ->
      ui.hideError()
      ui.showProgress()
    }

    return Observable
        .fromCallable {
          val entry = ongoingLoginEntry()

          activateUser.activate(userUuid, entry.pin!!)
        }
        .map(this::handleRequestLoginOtpResult)
        .startWith(showProgressBeforeRequestingOtp)
  }

  private fun handleRequestLoginOtpResult(result: ActivateUser.Result): UiChange {
    return { ui: Ui ->
      showMessageOnActivateUserResult(ui, result)
      hideProgressAfterRequestingOtp(ui)
    }
  }

  private fun loggedInUserUuid(): UUID {
    return userSession.loggedInUserImmediate()!!.uuid
  }

  private fun showMessageOnActivateUserResult(ui: Ui, result: ActivateUser.Result) {
    when (result) {
      is ActivateUser.Result.NetworkError -> ui.showNetworkError()
      is ActivateUser.Result.ServerError, is ActivateUser.Result.OtherError -> ui.showUnexpectedError()
      is ActivateUser.Result.Success -> ui.showSmsSentMessage()
    }
  }

  private fun hideProgressAfterRequestingOtp(ui: Ui) {
    ui.hideProgress()
    ui.clearPin()
  }

  private fun ongoingLoginEntry(): OngoingLoginEntry {
    return ongoingLoginEntryRepository.entryImmediate()
  }
}
