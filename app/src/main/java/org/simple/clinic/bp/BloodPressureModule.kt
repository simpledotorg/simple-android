package org.simple.clinic.bp

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.bp.sync.BloodPressureSyncApiV1
import org.simple.clinic.util.InstantRxPreferencesConverter
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import org.threeten.bp.Instant
import retrofit2.Retrofit
import javax.inject.Named

@Module
open class BloodPressureModule {

  @Provides
  fun dao(appDatabase: AppDatabase): BloodPressureMeasurement.RoomDao {
    return appDatabase.bloodPressureDao()
  }

  @Provides
  fun syncApi(retrofit: Retrofit): BloodPressureSyncApiV1 {
    return retrofit.create(BloodPressureSyncApiV1::class.java)
  }

  @Provides
  @Named("last_bp_pull_timestamp")
  fun lastPullTimestamp(rxSharedPrefs: RxSharedPreferences): Preference<Optional<Instant>> {
    return rxSharedPrefs.getObject("last_bp_pull_timestamp", None, OptionalRxPreferencesConverter(InstantRxPreferencesConverter()))
  }
}
