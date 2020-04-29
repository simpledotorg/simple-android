package org.simple.clinic.setup

import dagger.Module
import dagger.Provides
import org.simple.clinic.BuildConfig
import org.simple.clinic.appconfig.Country
import org.simple.clinic.di.AssistedInjectModule
import java.net.URI
import javax.inject.Named

@Module(includes = [AssistedInjectModule::class])
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
