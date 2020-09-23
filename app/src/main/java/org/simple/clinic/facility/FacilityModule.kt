package org.simple.clinic.facility

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import javax.inject.Named

@Module
class FacilityModule {

  @Provides
  fun facilityDao(appDatabase: AppDatabase): Facility.RoomDao {
    return appDatabase.facilityDao()
  }

  @Provides
  fun syncApi(@Named("for_country") retrofit: Retrofit): FacilitySyncApi {
    return retrofit.create(FacilitySyncApi::class.java)
  }

  @Provides
  @Named("last_facility_pull_token")
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getOptional("last_facility_pull_token_v2", StringPreferenceConverter())
  }
}
