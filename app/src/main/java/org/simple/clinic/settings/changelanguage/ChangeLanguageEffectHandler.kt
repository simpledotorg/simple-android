package org.simple.clinic.settings.changelanguage

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.settings.SettingsRepository
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.SyncTag.DAILY
import org.simple.clinic.util.scheduler.SchedulersProvider

class ChangeLanguageEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val settingsRepository: SettingsRepository,
    private val dataSync: DataSync,
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): ChangeLanguageEffectHandler
  }

  fun build(): ObservableTransformer<ChangeLanguageEffect, ChangeLanguageEvent> {
    return RxMobius
        .subtypeEffectHandler<ChangeLanguageEffect, ChangeLanguageEvent>()
        .addTransformer(LoadCurrentLanguageEffect::class.java, loadCurrentSelectedLanguage(settingsRepository, schedulersProvider.io()))
        .addTransformer(LoadSupportedLanguagesEffect::class.java, loadSupportedLanguages(settingsRepository, schedulersProvider.io()))
        .addTransformer(UpdateCurrentLanguageEffect::class.java, updateCurrentLanguage(settingsRepository, schedulersProvider.io()))
        .addAction(GoBack::class.java, uiActions::goBackToPreviousScreen, schedulersProvider.ui())
        .addAction(RestartActivity::class.java, uiActions::restartActivity, schedulersProvider.ui())
        .addAction(TriggerSync::class.java) { dataSync.fireAndForgetSync(DAILY) }
        .build()
  }

  private fun loadCurrentSelectedLanguage(
      settingsRepository: SettingsRepository,
      scheduler: Scheduler
  ): ObservableTransformer<LoadCurrentLanguageEffect, ChangeLanguageEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle {
            settingsRepository
                .getCurrentLanguage()
                .subscribeOn(scheduler)
          }
          .map(::CurrentLanguageLoadedEvent)
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

  private fun updateCurrentLanguage(
      settingsRepository: SettingsRepository,
      scheduler: Scheduler
  ): ObservableTransformer<UpdateCurrentLanguageEffect, ChangeLanguageEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .map { it.newLanguage }
          .flatMapSingle { newLanguage ->
            settingsRepository
                .setCurrentLanguage(newLanguage)
                .subscribeOn(scheduler)
                .toSingleDefault(newLanguage)
          }
          .map { CurrentLanguageChangedEvent }
    }
  }
}
