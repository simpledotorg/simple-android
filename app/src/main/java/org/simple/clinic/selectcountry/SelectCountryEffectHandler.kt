package org.simple.clinic.selectcountry

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.FetchError
import org.simple.clinic.appconfig.FetchSucceeded
import org.simple.clinic.appconfig.ManifestFetchResult
import org.simple.clinic.util.scheduler.SchedulersProvider

class SelectCountryEffectHandler @AssistedInject constructor(
    private val appConfigRepository: AppConfigRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<SelectCountryViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(viewEffectsConsumer: Consumer<SelectCountryViewEffect>): SelectCountryEffectHandler
  }

  fun build(): ObservableTransformer<SelectCountryEffect, SelectCountryEvent> {
    return RxMobius
        .subtypeEffectHandler<SelectCountryEffect, SelectCountryEvent>()
        .addTransformer(FetchManifest::class.java, fetchManifest())
        .addTransformer(SaveCountryEffect::class.java, saveCountry())
        .addConsumer(SelectCountryViewEffect::class.java) { viewEffect -> viewEffectsConsumer.accept(viewEffect) }
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
          .observeOn(schedulersProvider.io())
          .map { saveCountryEffect -> saveCountryEffect.country }
          .doOnNext(appConfigRepository::saveCurrentCountry)
          .map { CountrySaved }
    }
  }
}
