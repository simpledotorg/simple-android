package org.simple.clinic.facility.change

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.simple.clinic.util.Distance
import org.threeten.bp.Duration

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
}
