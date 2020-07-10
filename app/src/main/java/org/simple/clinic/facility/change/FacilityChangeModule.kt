package org.simple.clinic.facility.change

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.simple.clinic.util.Distance
import java.time.Duration
import javax.inject.Named

@Module
class FacilityChangeModule {

  @Provides
  fun config(): Observable<FacilityChangeConfig> {
    return Observable.just(FacilityChangeConfig(
        locationListenerExpiry = Duration.ofSeconds(5),
        locationUpdateInterval = Duration.ofSeconds(1),
        proximityThresholdForNearbyFacilities = Distance.ofKilometers(2.0),
        staleLocationThreshold = Duration.ofMinutes(10)))
  }

  @Provides
  @Named("is_facility_switched")
  fun isFacilitySwitchedPreference(rxSharedPrefs: RxSharedPreferences): Preference<Boolean> {
    return rxSharedPrefs.getBoolean("is_facility_switched", false)
  }
}
