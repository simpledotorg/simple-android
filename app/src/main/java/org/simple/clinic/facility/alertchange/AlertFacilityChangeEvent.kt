package org.simple.clinic.facility.alertchange

import org.simple.clinic.widgets.UiEvent

sealed interface AlertFacilityChangeEvent : UiEvent {

  data class IsFacilityChangedStatusLoaded(val isFacilityChanged: Boolean) : AlertFacilityChangeEvent

  data object FacilityChangedMarkedAsFalse : AlertFacilityChangeEvent

  data object YesButtonClicked : AlertFacilityChangeEvent {
    override val analyticsName: String = "Alert Facility Changed Sheet:Yes Button Clicked"
  }

  data object FacilityChanged : AlertFacilityChangeEvent
}
