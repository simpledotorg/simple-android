package org.simple.clinic.sync.indicator

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import org.simple.clinic.sync.LastSyncedState

@Module
class SyncIndicatorModule {

  @Provides
  fun lastFrequentSyncResult(
      rxSharedPreferences: RxSharedPreferences,
      moshi: Moshi
  ): Preference<LastSyncedState> {
    val typeConverter = LastSyncedState.RxPreferenceConverter(moshi)
    return rxSharedPreferences.getObject("last_frequent_sync_result_v1", LastSyncedState(), typeConverter)
  }

  @Provides
  fun syncIndicatorConfig(): SyncIndicatorConfig {
    return SyncIndicatorConfig.read()
  }
}
