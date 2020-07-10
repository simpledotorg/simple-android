package org.simple.clinic.facility.change

import org.simple.clinic.util.Distance
import java.time.Duration

data class FacilityChangeConfig(
    val locationListenerExpiry: Duration,
    val locationUpdateInterval: Duration,
    val proximityThresholdForNearbyFacilities: Distance,
    val staleLocationThreshold: Duration
)
