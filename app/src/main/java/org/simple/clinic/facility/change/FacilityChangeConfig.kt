package org.simple.clinic.facility.change

import org.simple.clinic.util.Distance
import org.threeten.bp.Duration

data class FacilityChangeConfig(
    val locationListenerExpiry: Duration,
    val locationUpdateInterval: Duration,
    val proximityThresholdForNearbyFacilities: Distance,
    val staleLocationThreshold: Duration
)
