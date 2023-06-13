package org.simple.clinic.appconfig.api

import io.reactivex.Single
import org.simple.clinic.BuildConfig
import org.simple.clinic.appconfig.Manifest
import retrofit2.http.GET

interface ManifestFetchApi {

  @GET(BuildConfig.MANIFEST_PATH)
  fun fetchManifest(): Single<Manifest>
}
