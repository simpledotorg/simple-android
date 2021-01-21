package org.simple.clinic.setup

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.BuildConfig
import org.simple.clinic.appconfig.Country
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.DatabaseMaintenanceRunAt
import org.simple.clinic.main.TypedPreference.Type.FallbackCountry
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.InstantRxPreferencesConverter
import org.simple.clinic.util.preference.getOptional
import java.net.URI
import java.time.Instant

@Module(includes = [
  SetupActivityConfigModule::class
])
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

  @Provides
  @TypedPreference(DatabaseMaintenanceRunAt)
  fun providesDatabaseMaintenanceRunAt(
      rxSharedPreferences: RxSharedPreferences
  ): Preference<Optional<Instant>> {
    return rxSharedPreferences.getOptional("database_maintenance_run_at", InstantRxPreferencesConverter())
  }
}
