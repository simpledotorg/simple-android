package org.simple.clinic.facility.change.confirm

import org.simple.clinic.facility.Facility

sealed class ConfirmFacilityChangeEffect

data class ChangeFacilityEffect(val selectedFacility: Facility) : ConfirmFacilityChangeEffect()

data object CloseSheet : ConfirmFacilityChangeEffect()

data object LoadCurrentFacility : ConfirmFacilityChangeEffect()

data object TouchFacilitySyncGroupSwitchedAtTime : ConfirmFacilityChangeEffect()
