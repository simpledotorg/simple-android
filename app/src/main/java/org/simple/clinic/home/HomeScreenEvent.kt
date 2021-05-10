package org.simple.clinic.home

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

sealed class HomeScreenEvent : UiEvent

object HomeFacilitySelectionClicked : HomeScreenEvent() {
  override val analyticsName = "Home Screen:Facility Clicked"
}

data class CurrentFacilityLoaded(val facility: Facility) : HomeScreenEvent()

data class OverdueAppointmentCountLoaded(val overdueAppointmentCount: Int) : HomeScreenEvent()
