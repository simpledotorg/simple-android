package org.simple.clinic.facility

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.user.LoggedInUserFacilityMapping
import org.simple.clinic.util.InstantRxPreferencesConverter
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import org.threeten.bp.Instant
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
  fun syncApi(retrofit: Retrofit): FacilitySyncApiV1 {
    return retrofit.create(FacilitySyncApiV1::class.java)
  }

  @Provides
  @Named("last_facility_pull_timestamp")
  fun lastPullTimestamp(rxSharedPrefs: RxSharedPreferences): Preference<Optional<Instant>> {
    return rxSharedPrefs.getObject("last_facility_pull_timestamp", None, OptionalRxPreferencesConverter(InstantRxPreferencesConverter()))
  }
}
