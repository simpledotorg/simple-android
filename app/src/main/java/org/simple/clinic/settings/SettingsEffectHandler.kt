package org.simple.clinic.settings

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.appupdate.AppUpdateState
import org.simple.clinic.appupdate.CheckAppUpdateAvailability
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserSession.LogoutResult.Success
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

class SettingsEffectHandler @AssistedInject constructor(
    private val userSession: UserSession,
    private val settingsRepository: SettingsRepository,
    private val schedulersProvider: SchedulersProvider,
    private val appVersionFetcher: AppVersionFetcher,
    private val appUpdateAvailability: CheckAppUpdateAvailability,
    @Assisted private val viewEffectsConsumer: Consumer<SettingsViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<SettingsViewEffect>
    ): SettingsEffectHandler
  }

  fun build(): ObservableTransformer<SettingsEffect, SettingsEvent> = RxMobius
      .subtypeEffectHandler<SettingsEffect, SettingsEvent>()
      .addTransformer(LoadUserDetailsEffect::class.java, loadUserDetails())
      .addTransformer(LoadCurrentLanguageEffect::class.java, loadCurrentSelectedLanguage())
      .addTransformer(LoadAppVersionEffect::class.java, loadAppVersion())
      .addTransformer(CheckAppUpdateAvailable::class.java, checkAppUpdateAvailability())
      .addConsumer(SettingsViewEffect::class.java, viewEffectsConsumer::accept)
      .addTransformer(LogoutUser::class.java, logoutUser())
      .build()

  private fun logoutUser(): ObservableTransformer<LogoutUser, SettingsEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMapSingle { userSession.logout() }
          .map(::UserLogoutResult)
    }
  }

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

  private fun loadAppVersion(): ObservableTransformer<LoadAppVersionEffect, SettingsEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .map { appVersionEffect ->
            val appVersionName = appVersionFetcher.appVersion()
            AppVersionLoaded(appVersionName)
          }
    }
  }

  private fun checkAppUpdateAvailability(): ObservableTransformer<CheckAppUpdateAvailable, SettingsEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .switchMap {
            appUpdateAvailability.listenAllUpdates()
          }
          .map {
            AppUpdateAvailabilityChecked(it is AppUpdateState.ShowAppUpdate)
          }
    }
  }
}
