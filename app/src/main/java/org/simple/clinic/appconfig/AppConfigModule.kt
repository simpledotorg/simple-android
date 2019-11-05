package org.simple.clinic.appconfig

import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.api.ManifestFetchApi
import retrofit2.Retrofit
import javax.inject.Named

@Module
class AppConfigModule {

  @Provides
  fun provideManifestFetchApi(@Named("for_config") retrofit: Retrofit): ManifestFetchApi {
    return retrofit.create(ManifestFetchApi::class.java)
  }
}
