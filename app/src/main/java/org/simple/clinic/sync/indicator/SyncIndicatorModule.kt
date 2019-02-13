package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.sync.EnumRxPreferenceConverter
import org.simple.clinic.sync.SyncProgress
import org.simple.clinic.util.InstantRxPreferencesConverter
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import org.threeten.bp.Instant
import javax.inject.Named

@Module
class SyncIndicatorModule {

  @Provides
  @Named("last_frequent_sync_succeeded_timestamp")
  fun lastFrequentSyncSucceededTimestamp(rxSharedPreferences: RxSharedPreferences): Preference<Optional<Instant>> {
    return rxSharedPreferences.getObject("last_frequent_sync_succeeded_timestamp", None, OptionalRxPreferencesConverter(InstantRxPreferencesConverter()))
  }

  @Provides
  @Named("last_frequent_sync_result")
  fun lastFrequentSyncResult(rxSharedPreferences: RxSharedPreferences): Preference<Optional<SyncProgress>> {
    return rxSharedPreferences.getObject("last_frequent_sync_result", None, OptionalRxPreferencesConverter(EnumRxPreferenceConverter(SyncProgress::class.java)))
  }
}
