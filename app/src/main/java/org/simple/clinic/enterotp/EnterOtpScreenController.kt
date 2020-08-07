package org.simple.clinic.enterotp

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.login.LoginUserWithOtp
import org.simple.clinic.login.activateuser.ActivateUser
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.OngoingLoginEntryRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.UiEvent
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

    return Observable.never()
  }
}
