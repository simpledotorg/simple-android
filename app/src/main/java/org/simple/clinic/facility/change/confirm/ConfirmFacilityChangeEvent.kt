package org.simple.clinic.facility.change.confirm

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

sealed class ConfirmFacilityChangeEvent : UiEvent

data class FacilityChangeConfirmed(val selectedFacility: Facility) : ConfirmFacilityChangeEvent()

data class FacilityChanged(val newFacility: Facility) : ConfirmFacilityChangeEvent()

data class CurrentFacilityLoaded(val currentFacility: Facility) : ConfirmFacilityChangeEvent()

object FacilitySyncGroupSwitchedAtTimeTouched : ConfirmFacilityChangeEvent()
