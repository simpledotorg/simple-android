package org.simple.clinic.settings

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

class SettingsEffectHandler @AssistedInject constructor(
    private val userSession: UserSession,
    private val settingsRepository: SettingsRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): SettingsEffectHandler
  }

  fun build(): ObservableTransformer<SettingsEffect, SettingsEvent> = RxMobius
      .subtypeEffectHandler<SettingsEffect, SettingsEvent>()
      .addTransformer(LoadUserDetailsEffect::class.java, loadUserDetails())
      .addTransformer(LoadCurrentLanguageEffect::class.java, loadCurrentSelectedLanguage())
      .addAction(OpenLanguageSelectionScreenEffect::class.java, uiActions::openLanguageSelectionScreen, schedulersProvider.ui())
      .build()

  private fun loadUserDetails(): ObservableTransformer<LoadUserDetailsEffect, SettingsEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .switchMap { userSession.loggedInUser().subscribeOn(schedulersProvider.io()) }
          .filterAndUnwrapJust()
          .map { user -> UserDetailsLoaded(user.fullName, user.phoneNumber) }
    }
  }

  private fun loadCurrentSelectedLanguage(): ObservableTransformer<LoadCurrentLanguageEffect, SettingsEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle { settingsRepository.getCurrentLanguage().subscribeOn(schedulersProvider.io()) }
          .map(::CurrentLanguageLoaded)
    }
  }
}
