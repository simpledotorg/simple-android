package org.simple.clinic.medicalhistory

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.medicalhistory.sync.MedicalHistorySyncApi
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.OptionalRxPreferencesConverter
import org.simple.clinic.util.preference.StringPreferenceConverter
import retrofit2.Retrofit
import javax.inject.Named

@Module
class MedicalHistoryModule {

  @Provides
  fun dao(appDatabase: AppDatabase): MedicalHistory.RoomDao {
    return appDatabase.medicalHistoryDao()
  }

  @Provides
  fun syncApi(retrofit: Retrofit): MedicalHistorySyncApi {
    return retrofit.create(MedicalHistorySyncApi::class.java)
  }

  @Provides
  @Named("last_medicalhistory_pull_token")
  fun lastPullTokenV2(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getObject("last_medicalhistory_pull_token_v2", None, OptionalRxPreferencesConverter(StringPreferenceConverter()))
  }
}
