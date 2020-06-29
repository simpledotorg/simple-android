package org.simple.clinic.registration.facility

import java.time.Duration

sealed class RegistrationFacilitySelectionEffect

data class FetchCurrentLocation(
    val updateInterval: Duration,
    val timeout: Duration,
    val discardOlderThan: Duration
): RegistrationFacilitySelectionEffect()

data class LoadFacilitiesWithQuery(val query: String) : RegistrationFacilitySelectionEffect()
