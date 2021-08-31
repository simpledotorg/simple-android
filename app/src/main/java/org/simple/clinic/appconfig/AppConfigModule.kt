package org.simple.clinic.appconfig

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import org.simple.clinic.appconfig.api.ManifestFetchApi
import org.simple.clinic.appconfig.displayname.CountryDisplayNameFetcherModule
import org.simple.clinic.util.preference.MoshiObjectPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import java.util.Optional
import javax.inject.Named

@Module(includes = [CountryDisplayNameFetcherModule::class])
class AppConfigModule {

  @Provides
  fun provideManifestFetchApi(@Named("for_config") retrofit: Retrofit): ManifestFetchApi {
    return retrofit.create(ManifestFetchApi::class.java)
  }

  @Provides
  fun provideSelectedCountryV2Preference(
      rxSharedPreferences: RxSharedPreferences,
      moshi: Moshi
  ): Preference<Optional<Country>> {
    val countryPreferenceConverter = MoshiObjectPreferenceConverter(moshi, Country::class.java)
    return rxSharedPreferences.getOptional("preference_selected_country_v2", countryPreferenceConverter)
  }

  @Provides
  fun provideSelectedDeployment(
      rxSharedPreferences: RxSharedPreferences,
      moshi: Moshi
  ): Preference<Optional<Deployment>> {
    val deploymentPreferenceConverter = MoshiObjectPreferenceConverter(moshi, Deployment::class.java)
    return rxSharedPreferences.getOptional("preference_selected_deployment_v1", deploymentPreferenceConverter)
  }
}
