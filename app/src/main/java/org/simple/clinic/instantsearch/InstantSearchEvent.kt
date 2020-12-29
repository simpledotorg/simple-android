package org.simple.clinic.instantsearch

import org.simple.clinic.facility.Facility

sealed class InstantSearchEvent

data class CurrentFacilityLoaded(val facility: Facility) : InstantSearchEvent()
