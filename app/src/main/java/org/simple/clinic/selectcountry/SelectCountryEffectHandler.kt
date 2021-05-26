package org.simple.clinic.selectcountry

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Completable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.FetchError
import org.simple.clinic.appconfig.FetchSucceeded
import org.simple.clinic.appconfig.ManifestFetchResult
import org.simple.clinic.util.scheduler.SchedulersProvider

class SelectCountryEffectHandler(
    private val appConfigRepository: AppConfigRepository,
    private val uiActions: UiActions,
    private val schedulersProvider: SchedulersProvider
) {

  companion object {
    fun create(
        appConfigRepository: AppConfigRepository,
        uiActions: UiActions,
        schedulersProvider: SchedulersProvider
    ): ObservableTransformer<SelectCountryEffect, SelectCountryEvent> {
      return SelectCountryEffectHandler(
          appConfigRepository,
          uiActions,
          schedulersProvider
      ).build()
    }
  }

  private fun build(): ObservableTransformer<SelectCountryEffect, SelectCountryEvent> {
    return RxMobius
        .subtypeEffectHandler<SelectCountryEffect, SelectCountryEvent>()
        .addTransformer(FetchManifest::class.java, fetchManifest())
        .addTransformer(SaveCountryEffect::class.java, saveCountry())
        .addAction(GoToNextScreen::class.java, uiActions::goToNextScreen, schedulersProvider.ui())
        .build()
  }

  private fun fetchManifest(): ObservableTransformer<FetchManifest, SelectCountryEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle { appConfigRepository.fetchAppManifest().subscribeOn(schedulersProvider.io()) }
          .map(::manifestFetchResultToEvent)
    }
  }

  private fun manifestFetchResultToEvent(fetchResult: ManifestFetchResult): SelectCountryEvent {
    return when (fetchResult) {
      is FetchSucceeded -> ManifestFetched(fetchResult.countries)
      is FetchError -> ManifestFetchFailed(ManifestFetchError.fromResolvedError(fetchResult.error))
    }
  }

  private fun saveCountry(): ObservableTransformer<SaveCountryEffect, SelectCountryEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .map { saveCountryEffect -> saveCountryEffect.country }
          .flatMapSingle { countryToSave ->
            persistSelectedCountry(countryToSave)
                .andThen(Single.just(CountrySaved))
          }
    }
  }

  private fun persistSelectedCountry(countryToSave: Country): Completable {
    return appConfigRepository
        .saveCurrentCountry(countryToSave)
        .subscribeOn(schedulersProvider.io())
  }
}
