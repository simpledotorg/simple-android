package org.simple.clinic.registration.facility

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

class RegistrationFacilitySelectionRetryClicked : UiEvent

data class RegistrationFacilitySelectionChanged(val facility: Facility, val isSelected: Boolean) : UiEvent

data class RegistrationSelectedFacilitiesChanged(val selectedFacilities: Set<Facility>) : UiEvent

class RegistrationFacilitySelectionDoneClicked : UiEvent
