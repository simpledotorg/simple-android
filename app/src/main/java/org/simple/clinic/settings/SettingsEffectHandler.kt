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
      settingsRepository: SettingsRepository,
      schedulersProvider: SchedulersProvider,
      uiActions: UiActions
  ): ObservableTransformer<SettingsEffect, SettingsEvent> {
    return RxMobius
        .subtypeEffectHandler<SettingsEffect, SettingsEvent>()
        .addTransformer(LoadUserDetailsEffect::class.java, loadUserDetails(userSession, schedulersProvider.io()))
        .addTransformer(LoadCurrentLanguageEffect::class.java, loadCurrentSelectedLanguage(settingsRepository, schedulersProvider.io()))
        .addAction(OpenLanguageSelectionScreenEffect::class.java, uiActions::openLanguageSelectionScreen, schedulersProvider.ui())
        .build()
  }

  private fun loadUserDetails(
      userSession: UserSession,
      scheduler: Scheduler
  ): ObservableTransformer<LoadUserDetailsEffect, SettingsEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .switchMap { userSession.loggedInUser().subscribeOn(scheduler) }
          .filterAndUnwrapJust()
          .map { user -> UserDetailsLoaded(user.fullName, user.phoneNumber) }
    }
  }

  private fun loadCurrentSelectedLanguage(
      settingsRepository: SettingsRepository,
      scheduler: Scheduler
  ): ObservableTransformer<LoadCurrentLanguageEffect, SettingsEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle { settingsRepository.getCurrentLanguage().subscribeOn(scheduler) }
          .map(::CurrentLanguageLoaded)
    }
  }
}
