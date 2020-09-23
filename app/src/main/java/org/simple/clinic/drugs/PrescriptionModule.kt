package org.simple.clinic.drugs

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.drugs.sync.PrescriptionSyncApi
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import javax.inject.Named

@Module
class PrescriptionModule {

  @Provides
  fun dao(appDatabase: AppDatabase): PrescribedDrug.RoomDao {
    return appDatabase.prescriptionDao()
  }

  @Provides
  fun syncApi(@Named("for_country") retrofit: Retrofit): PrescriptionSyncApi {
    return retrofit.create(PrescriptionSyncApi::class.java)
  }

  @Provides
  @Named("last_prescription_pull_token")
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getOptional("last_prescription_pull_token_v2", StringPreferenceConverter())
  }
}
