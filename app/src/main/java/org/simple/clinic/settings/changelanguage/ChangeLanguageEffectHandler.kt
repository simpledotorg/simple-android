package org.simple.clinic.settings.changelanguage

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.settings.SettingsRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

object ChangeLanguageEffectHandler {

  fun create(
      schedulersProvider: SchedulersProvider,
      settingsRepository: SettingsRepository
  ): ObservableTransformer<ChangeLanguageEffect, ChangeLanguageEvent> {
    return RxMobius
        .subtypeEffectHandler<ChangeLanguageEffect, ChangeLanguageEvent>()
        .addTransformer(LoadCurrentSelectedLanguageEffect::class.java) { effectStream ->
          effectStream
              .flatMapSingle {
                settingsRepository
                    .getCurrentSelectedLanguage()
                    .subscribeOn(schedulersProvider.io())
              }
              .map(::CurrentSelectedLanguageLoadedEvent)
        }
        .addTransformer(LoadSupportedLanguagesEffect::class.java) { effectStream ->
          effectStream
              .flatMapSingle {
                settingsRepository
                    .getSupportedLanguages()
                    .subscribeOn(schedulersProvider.io())
              }
              .map(::SupportedLanguagesLoadedEvent)
        }
        .addTransformer(UpdateSelectedLanguageEffect::class.java) { effectStream ->
          effectStream
              .map { it.newLanguage }
              .flatMapSingle { newLanguage ->
                settingsRepository
                    .setCurrentSelectedLanguage(newLanguage)
                    .subscribeOn(schedulersProvider.io())
                    .toSingleDefault(newLanguage)
              }
              .map(::SelectedLanguageChangedEvent)
        }
        .build()
  }
}
