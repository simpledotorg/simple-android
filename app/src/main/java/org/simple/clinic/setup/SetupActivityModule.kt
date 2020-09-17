package org.simple.clinic.setup

import dagger.Module
import dagger.Provides
import org.simple.clinic.BuildConfig
import org.simple.clinic.appconfig.Country
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.FallbackCountry
import java.net.URI

@Module(includes = [AssistedInjectModule::class])
class SetupActivityModule {

  @Provides
  @TypedPreference(FallbackCountry)
  fun providesFallbackCountry(): Country {
    return Country(
        isoCountryCode = "IN",
        endpoint = URI.create(BuildConfig.FALLBACK_ENDPOINT),
        displayName = "India",
        isdCode = "91"
    )
  }
}
