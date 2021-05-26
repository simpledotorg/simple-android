package org.simple.clinic.facilitypicker

import org.simple.clinic.facility.Facility
import java.time.Duration

sealed class FacilityPickerEffect

data class FetchCurrentLocation(
    val updateInterval: Duration,
    val timeout: Duration,
    val discardOlderThan: Duration
) : FacilityPickerEffect()

data class LoadFacilitiesWithQuery(val query: String) : FacilityPickerEffect()

object LoadTotalFacilityCount : FacilityPickerEffect()

data class ForwardSelectedFacility(val facility: Facility) : FacilityPickerEffect()

data class LoadFacilitiesInCurrentGroup(val query: String) : FacilityPickerEffect()
