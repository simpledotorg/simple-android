package org.simple.clinic.bloodsugar.di

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.sync.BloodSugarSyncApi
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import javax.inject.Named

@Module
class BloodSugarModule {

  @Provides
  fun dao(appDatabase: AppDatabase): BloodSugarMeasurement.RoomDao {
    return appDatabase.bloodSugarDao()
  }

  @Provides
  fun syncApi(@Named("for_country") retrofit: Retrofit): BloodSugarSyncApi {
    return retrofit.create(BloodSugarSyncApi::class.java)
  }

  @Provides
  @Named("last_blood_sugar_pull_token")
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getOptional("last_blood_sugar_pull_token", StringPreferenceConverter())
  }
}
