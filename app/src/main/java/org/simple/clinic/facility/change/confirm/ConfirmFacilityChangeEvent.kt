package org.simple.clinic.facility.change.confirm

import org.simple.clinic.facility.Facility

sealed class ConfirmFacilityChangeEvent

data class FacilityChangeConfirmed(val selectedFacility: Facility) : ConfirmFacilityChangeEvent()

object FacilityChanged : ConfirmFacilityChangeEvent()
