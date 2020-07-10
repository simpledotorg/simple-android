package org.simple.clinic.registration

import org.simple.clinic.util.Distance
import java.time.Duration

data class RegistrationConfig(
    val locationListenerExpiry: Duration,
    val locationUpdateInterval: Duration,
    val proximityThresholdForNearbyFacilities: Distance,
    val staleLocationThreshold: Duration
)
