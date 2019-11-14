package org.simple.clinic.setup

import dagger.Module
import dagger.Provides
import org.simple.clinic.BuildConfig
import org.simple.clinic.appconfig.Country
import java.net.URI
import javax.inject.Named

@Module
class SetupActivityModule {

  @Provides
  @Named("fallback")
  fun providesFallbackCountry(): Country {
    return Country(
        isoCountryCode = "IN",
        endpoint = URI.create(BuildConfig.FALLBACK_ENDPOINT),
        displayName = "India",
        isdCode = "91"
    )
  }
}
