package org.simple.clinic.facility.change

import org.simple.clinic.facility.Facility

sealed class FacilityChangeEffect

data object LoadCurrentFacility : FacilityChangeEffect()

data class OpenConfirmFacilityChangeSheet(val facility: Facility) : FacilityChangeEffect()

data object GoBack : FacilityChangeEffect()
