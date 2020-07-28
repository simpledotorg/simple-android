package org.simple.clinic.facility.change

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class FacilityChangeModule {

  @Provides
  @Named("is_facility_switched")
  fun isFacilitySwitchedPreference(rxSharedPrefs: RxSharedPreferences): Preference<Boolean> {
    return rxSharedPrefs.getBoolean("is_facility_switched", false)
  }
}
