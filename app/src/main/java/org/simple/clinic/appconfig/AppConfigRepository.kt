package org.simple.clinic.appconfig

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.appconfig.api.ManifestFetchApi
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.toNullable
import java.util.Optional
import javax.inject.Inject

/**
 * This class is responsible for providing access to the current configuration in the app. This
 * includes:
 *
 * - What is the current selected [Country_Old] (if any?)
 * - Fetching the list of supported countries from the server
 * - Updating the current selected country
 **/
class AppConfigRepository @Inject constructor(
    private val manifestFetchApi: ManifestFetchApi,
    private val selectedCountryOldPreference: Preference<Optional<Country_Old>>,
    private val selectedCountryPreference: Preference<Optional<Country>>,
    private val selectedDeployment: Preference<Optional<Deployment>>
) {

  fun currentCountry_Old(): Optional<Country_Old> {
    return selectedCountryOldPreference.get()
  }

  fun currentCountryObservable(): Observable<Optional<Country>> {
    return selectedCountryPreference.asObservable()
  }

  fun currentDeployment(): Deployment? {
    return selectedDeployment.get().toNullable()
  }

  fun currentCountry(): Country? {
    return selectedCountryPreference.get().toNullable()
  }

  fun fetchAppManifest(): Single<ManifestFetchResult> {
    return manifestFetchApi
        .fetchManifest()
        .map { it.supportedCountries }
        .map { FetchSucceeded(it.countries) }
        .cast(ManifestFetchResult::class.java)
        .onErrorReturn { cause -> FetchError(ErrorResolver.resolve(cause)) }
  }

  fun saveCurrentCountry(countryOld: Country_Old): Completable {
    return Completable.fromAction { selectedCountryOldPreference.set(Optional.of(countryOld)) }
  }

  fun saveCurrentCountry(country: Country) {
    selectedCountryPreference.set(Optional.of(country))
  }

  fun saveDeployment(deployment: Deployment) {
    selectedDeployment.set(Optional.of(deployment))
  }

  fun deleteV1Country() {
    selectedCountryOldPreference.delete()
  }
}
