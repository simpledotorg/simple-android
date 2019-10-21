package org.simple.clinic.encounter

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.encounter.sync.EncounterSyncApi
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.OptionalRxPreferencesConverter
import org.simple.clinic.util.preference.StringPreferenceConverter
import retrofit2.Retrofit
import javax.inject.Named

@Module
class EncounterModule {

  @Provides
  fun syncApi(retrofit: Retrofit): EncounterSyncApi {
    return retrofit.create(EncounterSyncApi::class.java)
  }

  @Provides
  @Named("last_encounter_pull_token")
  fun lastPullToken(rxSharedPreferences: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPreferences.getObject("last_encounter_pull_token", None, OptionalRxPreferencesConverter(StringPreferenceConverter()))
  }
}
