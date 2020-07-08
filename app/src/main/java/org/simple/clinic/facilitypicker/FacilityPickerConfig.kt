package org.simple.clinic.facilitypicker

import org.simple.clinic.util.Distance
import java.time.Duration
import javax.inject.Inject

data class FacilityPickerConfig(
    val locationListenerExpiry: Duration,
    val locationUpdateInterval: Duration,
    val proximityThresholdForNearbyFacilities: Distance,
    val staleLocationThreshold: Duration
) {

  @Inject
  constructor() : this(
      locationListenerExpiry = Duration.ofSeconds(5),
      locationUpdateInterval = Duration.ofSeconds(1),
      proximityThresholdForNearbyFacilities = Distance.ofKilometers(2.0),
      staleLocationThreshold = Duration.ofMinutes(10)
  )
}
