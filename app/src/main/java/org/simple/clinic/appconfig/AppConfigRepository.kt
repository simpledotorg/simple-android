package org.simple.clinic.appconfig

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.BuildConfig
import org.simple.clinic.appconfig.api.ManifestFetchApi
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import java.net.URI
import javax.inject.Inject

/**
 * This class is responsible for providing access to the current configuration in the app. This
 * includes:
 *
 * - What is the current selected [Country] (if any?)
 * - Fetching the list of supported countries from the server
 * - Updating the current selected country
 **/
class AppConfigRepository @Inject constructor(private val manifestFetchApi: ManifestFetchApi) {

  fun currentCountry(): Optional<Country> {

    // This has been temporarily hardcoded to build features which need access to the current
    // selected country in parallel while the country selection feature is being implemented.
    // This hardcoding will get replaced later when that feature is completed.
    // TODO(vs): 2019-10-31 Read this from storage instead of hardcoding
    return Just(Country(
        code = "IN",
        endpoint = URI(BuildConfig.API_ENDPOINT),
        displayName = "India",
        isdCode = "91"
    ))
  }

  fun fetchAppManifest(): Single<ManifestFetchResult> {
    return manifestFetchApi
        .fetchManifest()
        .map(::FetchSucceeded)
        .cast(ManifestFetchResult::class.java)
        .onErrorReturn { cause -> FetchError(ErrorResolver.resolve(cause)) }
  }

  fun saveCurrentCountry(country: Country): Completable {
    TODO("not implemented")
  }
}
