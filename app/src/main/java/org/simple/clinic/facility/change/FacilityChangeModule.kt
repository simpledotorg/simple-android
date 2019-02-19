package org.simple.clinic.facility.change

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.util.Distance
import org.threeten.bp.Duration

@Module
class FacilityChangeModule {

  @Provides
  fun config(): Single<FacilityChangeConfig> {
    return Single.just(FacilityChangeConfig(
        locationListenerExpiry = Duration.ofSeconds(5),
        locationUpdateInterval = Duration.ofSeconds(1),
        proximityThresholdForNearbyFacilities = Distance.ofKilometers(2.0),
        staleLocationThreshold = Duration.ofMinutes(10)))
  }
}
