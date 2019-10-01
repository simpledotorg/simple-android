package org.simple.clinic.settings

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

object SettingsEffectHandler {

  fun create(
      userSession: UserSession,
      schedulersProvider: SchedulersProvider
  ): ObservableTransformer<SettingsEffect, SettingsEvent> {
    return RxMobius
        .subtypeEffectHandler<SettingsEffect, SettingsEvent>()
        .addTransformer(LoadUserDetailsEffect::class.java, loadUserDetails(userSession, schedulersProvider.io()))
        .build()
  }

  private fun loadUserDetails(userSession: UserSession, scheduler: Scheduler): ObservableTransformer<LoadUserDetailsEffect, SettingsEvent> {
    return ObservableTransformer { upstream ->
      upstream
          .switchMap { userSession.loggedInUser().subscribeOn(scheduler) }
          .filterAndUnwrapJust()
          .map { user -> UserDetailsLoaded(user.fullName, user.phoneNumber) }
    }
  }
}
