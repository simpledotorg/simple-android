package org.simple.clinic.medicalhistory

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.medicalhistory.sync.MedicalHistorySyncApiV1
import org.simple.clinic.util.InstantRxPreferencesConverter
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import org.threeten.bp.Instant
import retrofit2.Retrofit
import javax.inject.Named

@Module
class MedicalHistoryModule {

  @Provides
  fun dao(appDatabase: AppDatabase): MedicalHistory.RoomDao {
    return appDatabase.medicalHistoryDao()
  }

  @Provides
  fun syncApi(retrofit: Retrofit): MedicalHistorySyncApiV1 {
    return retrofit.create(MedicalHistorySyncApiV1::class.java)
  }

  @Provides
  @Named("last_medicalhistory_pull_timestamp")
  fun lastPullTimestamp(rxSharedPrefs: RxSharedPreferences): Preference<Optional<Instant>> {
    return rxSharedPrefs.getObject("last_medicalhistory_pull_timestamp", None, OptionalRxPreferencesConverter(InstantRxPreferencesConverter()))
  }
}
