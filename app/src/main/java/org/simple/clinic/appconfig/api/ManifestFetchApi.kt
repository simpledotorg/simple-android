package org.simple.clinic.appconfig.api

import io.reactivex.Single
import org.simple.clinic.appconfig.Country
import retrofit2.http.GET

interface ManifestFetchApi {

  @GET("manifest.json")
  fun fetchManifest(): Single<List<Country>>
}
