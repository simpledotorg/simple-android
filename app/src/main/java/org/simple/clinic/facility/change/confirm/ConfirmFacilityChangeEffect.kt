package org.simple.clinic.facility.change.confirm

import org.simple.clinic.facility.Facility

sealed class ConfirmFacilityChangeEffect

data class ChangeFacilityEffect(val selectedFacility: Facility) : ConfirmFacilityChangeEffect()

object CloseSheet : ConfirmFacilityChangeEffect()

object LoadCurrentFacility : ConfirmFacilityChangeEffect()

object TouchFacilitySyncGroupSwitchedAtTime : ConfirmFacilityChangeEffect()
