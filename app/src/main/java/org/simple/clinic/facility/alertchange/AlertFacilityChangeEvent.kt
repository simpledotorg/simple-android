package org.simple.clinic.facility.alertchange

sealed interface AlertFacilityChangeEvent {
  data class IsFacilityChangedStatusLoaded(val isFacilityChanged: Boolean) : AlertFacilityChangeEvent

  data object FacilityChangedMarkedAsFalse : AlertFacilityChangeEvent

  data object YesButtonClicked : AlertFacilityChangeEvent
}
