package org.simple.clinic.patient.sync

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import org.simple.clinic.util.StringPreferenceConverter
import retrofit2.Retrofit
import javax.inject.Named

@Module
open class PatientSyncModule {

  @Provides
  fun api(retrofit: Retrofit): PatientSyncApi {
    return retrofit.create(PatientSyncApi::class.java)
  }

  @Provides
  @Named("last_patient_pull_token")
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getObject("last_patient_pull_token_v3", None, OptionalRxPreferencesConverter(StringPreferenceConverter()))
  }
}
