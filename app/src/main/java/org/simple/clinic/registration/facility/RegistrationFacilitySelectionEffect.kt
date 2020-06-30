package org.simple.clinic.registration.facility

import java.time.Duration
import org.simple.clinic.facility.Facility

sealed class RegistrationFacilitySelectionEffect

data class FetchCurrentLocation(
    val updateInterval: Duration,
    val timeout: Duration,
    val discardOlderThan: Duration
): RegistrationFacilitySelectionEffect()

data class LoadFacilitiesWithQuery(val query: String) : RegistrationFacilitySelectionEffect()

object LoadTotalFacilityCount: RegistrationFacilitySelectionEffect()

data class OpenConfirmFacilitySheet(val facility: Facility): RegistrationFacilitySelectionEffect()
