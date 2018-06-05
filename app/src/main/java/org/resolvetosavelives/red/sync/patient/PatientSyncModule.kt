package org.resolvetosavelives.red.sync.patient

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.resolvetosavelives.red.util.InstantRxPreferencesConverter
import org.resolvetosavelives.red.util.None
import org.resolvetosavelives.red.util.Optional
import org.resolvetosavelives.red.util.OptionalRxPreferencesConverter
import org.threeten.bp.Instant
import retrofit2.Retrofit
import javax.inject.Named

@Module
open class PatientSyncModule {

  @Provides
  fun api(@Named("RedApp") retrofit: Retrofit): PatientSyncApiV1 {
    return retrofit.create(PatientSyncApiV1::class.java)
  }

  @Provides
  @Named("last_patient_pull_timestamp")
  fun lastPullTimestamp(rxSharedPrefs: RxSharedPreferences): Preference<Optional<Instant>> {
    return rxSharedPrefs.getObject("last_patient_pull_timestamp", None, OptionalRxPreferencesConverter(InstantRxPreferencesConverter()))
  }
}
