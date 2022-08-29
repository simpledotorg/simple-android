package org.simple.clinic.setup

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.DatabaseMaintenanceRunAt
import org.simple.clinic.util.preference.InstantRxPreferencesConverter
import org.simple.clinic.util.preference.getOptional
import java.time.Instant
import java.util.Optional

@Module(includes = [
  SetupActivityConfigModule::class
])
class SetupActivityModule {

  @Provides
  @TypedPreference(DatabaseMaintenanceRunAt)
  fun providesDatabaseMaintenanceRunAt(
      rxSharedPreferences: RxSharedPreferences
  ): Preference<Optional<Instant>> {
    return rxSharedPreferences.getOptional("database_maintenance_run_at", InstantRxPreferencesConverter())
  }
}
