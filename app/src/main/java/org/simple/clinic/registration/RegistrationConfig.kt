package org.simple.clinic.registration

import org.simple.clinic.util.Distance
import org.threeten.bp.Duration

data class RegistrationConfig(
    val locationListenerExpiry: Duration,
    val locationUpdateInterval: Duration,
    val proximityThresholdForNearbyFacilities: Distance,
    val staleLocationThreshold: Duration
)
