package org.simple.clinic.registration.phone.loggedout

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class LoggedOutOfDeviceEffectHandler @Inject constructor(
    private val userSession: UserSession,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<LoggedOutOfDeviceEffect, LoggedOutOfDeviceEvent> = RxMobius
      .subtypeEffectHandler<LoggedOutOfDeviceEffect, LoggedOutOfDeviceEvent>()
      .addTransformer(LogoutUser::class.java, logoutUser())
      .build()

  private fun logoutUser(): ObservableTransformer<LogoutUser, LoggedOutOfDeviceEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMapSingle { userSession.logout() }
          .map(::UserLoggedOut)
    }
  }
}
