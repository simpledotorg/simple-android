package org.simple.clinic.registration.facility

import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.widgets.UiEvent

sealed class RegistrationFacilitySelectionEvent : UiEvent

data class LocationFetched(val update: LocationUpdate) : RegistrationFacilitySelectionEvent()
