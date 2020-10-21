package org.simple.clinic.facility.change.confirm

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

sealed class ConfirmFacilityChangeEvent : UiEvent

data class FacilityChangeConfirmed(val selectedFacility: Facility) : ConfirmFacilityChangeEvent()

object FacilityChanged : ConfirmFacilityChangeEvent()

data class CurrentFacilityLoaded(val currentFacility: Facility): ConfirmFacilityChangeEvent()
