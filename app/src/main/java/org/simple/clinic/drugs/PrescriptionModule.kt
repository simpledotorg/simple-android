package org.simple.clinic.drugs

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.drugs.sync.PrescriptionSyncApi
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.OptionalRxPreferencesConverter
import org.simple.clinic.util.preference.StringPreferenceConverter
import retrofit2.Retrofit
import javax.inject.Named

@Module
class PrescriptionModule {

  @Provides
  fun dao(appDatabase: AppDatabase): PrescribedDrug.RoomDao {
    return appDatabase.prescriptionDao()
  }

  @Provides
  fun syncApi(retrofit: Retrofit): PrescriptionSyncApi {
    return retrofit.create(PrescriptionSyncApi::class.java)
  }

  @Provides
  @Named("last_prescription_pull_token")
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getObject("last_prescription_pull_token_v2", None, OptionalRxPreferencesConverter(StringPreferenceConverter()))
  }
}
