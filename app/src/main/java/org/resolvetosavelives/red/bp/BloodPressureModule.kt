package org.resolvetosavelives.red.bp

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.bp.sync.BloodPressureSyncApiV1
import org.resolvetosavelives.red.util.InstantRxPreferencesConverter
import org.resolvetosavelives.red.util.None
import org.resolvetosavelives.red.util.Optional
import org.resolvetosavelives.red.util.OptionalRxPreferencesConverter
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
  fun syncApi(@Named("RedApp") retrofit: Retrofit): BloodPressureSyncApiV1 {
    return retrofit.create(BloodPressureSyncApiV1::class.java)
  }

  @Provides
  @Named("last_bp_pull_timestamp")
  fun lastPullTimestamp(rxSharedPrefs: RxSharedPreferences): Preference<Optional<Instant>> {
    return rxSharedPrefs.getObject("last_patient_pull_timestamp", None, OptionalRxPreferencesConverter(InstantRxPreferencesConverter()))
  }
}
