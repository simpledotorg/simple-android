package org.simple.clinic.appconfig

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.appconfig.StatesResult.StatesFetched
import org.simple.clinic.appconfig.api.ManifestFetchApi
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.CountryV1
import org.simple.clinic.main.TypedPreference.Type.SelectedState
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.toNullable
import java.util.Optional
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
    private val selectedCountryPreference: Preference<Optional<Country>>,
    private val selectedDeployment: Preference<Optional<Deployment>>,
    @TypedPreference(SelectedState) private val selectedStatePreference: Preference<Optional<String>>,
    private val statesFetcher: StatesFetcher,
    @TypedPreference(CountryV1) private val countryV1Preference: Preference<Optional<String>>
) {

  fun currentCountryObservable(): Observable<Optional<Country>> {
    return selectedCountryPreference.asObservable()
  }

  fun currentDeployment(): Deployment? {
    return selectedDeployment.get().toNullable()
  }

  fun currentDeploymentObservable(): Observable<Optional<Deployment>> {
    return selectedDeployment.asObservable()
  }

  fun currentCountry(): Country? {
    return selectedCountryPreference.get().toNullable()
  }

  fun currentState(): String? {
    return selectedStatePreference.get().toNullable()
  }

  fun fetchAppManifest(): Single<ManifestFetchResult> {
    return manifestFetchApi
        .fetchManifest()
        .map { it.supportedCountries }
        .map { FetchSucceeded(it.countries) }
        .cast(ManifestFetchResult::class.java)
        .onErrorReturn { cause -> FetchError(ErrorResolver.resolve(cause)) }
  }

  fun saveCurrentCountry(country: Country) {
    selectedCountryPreference.set(Optional.of(country))
  }

  fun saveDeployment(deployment: Deployment) {
    selectedDeployment.set(Optional.of(deployment))
  }

  fun saveState(state: State) {
    selectedStatePreference.set(Optional.of(state.displayName))
  }

  fun fetchStatesInSelectedCountry(): StatesResult {
    val selectedCountry = selectedCountryPreference.get().get()

    return try {
      val states = selectedCountry.deployments
          .flatMap(statesFetcher::fetchStates)
          .distinctBy { state -> state.displayName }
          .sortedBy { state -> state.displayName }

      StatesFetched(states)
    } catch (e: Exception) {
      StatesResult.FetchError(ErrorResolver.resolve(e))
    }
  }

  fun deleteStoredCountryV1() {
    countryV1Preference.delete()
  }
}
