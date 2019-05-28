package org.simple.clinic.facility

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.user.LoggedInUserFacilityMapping
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import org.simple.clinic.util.StringPreferenceConverter
import retrofit2.Retrofit
import javax.inject.Named

@Module
class FacilityModule {

  @Provides
  fun facilityDao(appDatabase: AppDatabase): Facility.RoomDao {
    return appDatabase.facilityDao()
  }

  @Provides
  fun userFacilityMappingDao(appDatabase: AppDatabase): LoggedInUserFacilityMapping.RoomDao {
    return appDatabase.userFacilityMappingDao()
  }

  @Provides
  fun syncApi(retrofit: Retrofit): FacilitySyncApi {
    return retrofit.create(FacilitySyncApi::class.java)
  }

  @Provides
  @Named("last_facility_pull_token")
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getObject("last_facility_pull_token_v2", None, OptionalRxPreferencesConverter(StringPreferenceConverter()))
  }
}
