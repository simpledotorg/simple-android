package org.simple.clinic.settings.changelanguage

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.settings.SettingsRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

object ChangeLanguageEffectHandler {

  fun create(
      schedulersProvider: SchedulersProvider,
      settingsRepository: SettingsRepository,
      activityRestarter: ActivityRestarter
  ): ObservableTransformer<ChangeLanguageEffect, ChangeLanguageEvent> {
    return RxMobius
        .subtypeEffectHandler<ChangeLanguageEffect, ChangeLanguageEvent>()
        .addTransformer(LoadCurrentSelectedLanguageEffect::class.java, loadCurrentSelectedLanguage(settingsRepository, schedulersProvider.io()))
        .addTransformer(LoadSupportedLanguagesEffect::class.java, loadSupportedLanguages(settingsRepository, schedulersProvider.io()))
        .addTransformer(UpdateSelectedLanguageEffect::class.java, updateSelectedLanguage(settingsRepository, schedulersProvider.io()))
        .addAction(RestartActivityEffect::class.java, activityRestarter::restart, schedulersProvider.ui())
        .build()
  }

  private fun loadCurrentSelectedLanguage(
      settingsRepository: SettingsRepository,
      scheduler: Scheduler
  ): ObservableTransformer<LoadCurrentSelectedLanguageEffect, ChangeLanguageEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle {
            settingsRepository
                .getCurrentSelectedLanguage()
                .subscribeOn(scheduler)
          }
          .map(::CurrentSelectedLanguageLoadedEvent)
    }
  }

  private fun loadSupportedLanguages(
      settingsRepository: SettingsRepository,
      scheduler: Scheduler
  ): ObservableTransformer<LoadSupportedLanguagesEffect, ChangeLanguageEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle {
            settingsRepository
                .getSupportedLanguages()
                .subscribeOn(scheduler)
          }
          .map(::SupportedLanguagesLoadedEvent)
    }
  }

  private fun updateSelectedLanguage(
      settingsRepository: SettingsRepository,
      scheduler: Scheduler
  ): ObservableTransformer<UpdateSelectedLanguageEffect, ChangeLanguageEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .map { it.newLanguage }
          .flatMapSingle { newLanguage ->
            settingsRepository
                .setCurrentSelectedLanguage(newLanguage)
                .subscribeOn(scheduler)
                .toSingleDefault(newLanguage)
          }
          .map(::SelectedLanguageChangedEvent)
    }
  }

  interface ActivityRestarter {
    fun restart()
  }
}
