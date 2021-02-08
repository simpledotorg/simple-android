package org.simple.clinic.appconfig

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.appconfig.api.ManifestFetchApi
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toOptional
import javax.inject.Inject

/**
 * This class is responsible for providing access to the current configuration in the app. This
 * includes:
 *
 * - What is the current selected [Country] (if any?)
 * - Fetching the list of supported countries from the server
 * - Updating the current selected country
 **/
class AppConfigRepository @Inject constructor(
    private val manifestFetchApi: ManifestFetchApi,
    private val selectedCountryPreference: Preference<Optional<Country>>
) {

  fun currentCountry(): Optional<Country> {
    return selectedCountryPreference.get()
  }

  fun currentCountryObservable(): Observable<Optional<Country>> {
    return selectedCountryPreference.asObservable()
  }

  fun fetchAppManifest(): Single<ManifestFetchResult> {
    return manifestFetchApi
        .fetchManifest()
        .map { it.supportedCountries }
        .map(::FetchSucceeded)
        .cast(ManifestFetchResult::class.java)
        .onErrorReturn { cause -> FetchError(ErrorResolver.resolve(cause)) }
  }

  fun saveCurrentCountry(country: Country): Completable {
    return Completable.fromAction { selectedCountryPreference.set(Just(country)) }
  }
}
