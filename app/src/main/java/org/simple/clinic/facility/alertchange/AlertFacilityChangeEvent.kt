package org.simple.clinic.facility.alertchange

sealed interface AlertFacilityChangeEvent {
  data class IsFacilityChangedStatusLoaded(val isFacilityChanged: Boolean) : AlertFacilityChangeEvent
}
