package org.simple.clinic.setup

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.Provides
import org.simple.clinic.BuildConfig
import org.simple.clinic.appconfig.Country
import java.net.URI
import javax.inject.Named

@AssistedModule
@Module(includes = [AssistedInject_SetupActivityModule::class])
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
