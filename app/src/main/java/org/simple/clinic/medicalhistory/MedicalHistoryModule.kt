package org.simple.clinic.medicalhistory

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.medicalhistory.sync.MedicalHistorySyncApi
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import javax.inject.Named

@Module
class MedicalHistoryModule {

  @Provides
  fun dao(appDatabase: AppDatabase): MedicalHistory.RoomDao {
    return appDatabase.medicalHistoryDao()
  }

  @Provides
  fun syncApi(@Named("for_country") retrofit: Retrofit): MedicalHistorySyncApi {
    return retrofit.create(MedicalHistorySyncApi::class.java)
  }

  @Provides
  @Named("last_medicalhistory_pull_token")
  fun lastPullTokenV2(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getOptional("last_medicalhistory_pull_token_v2", StringPreferenceConverter())
  }
}
