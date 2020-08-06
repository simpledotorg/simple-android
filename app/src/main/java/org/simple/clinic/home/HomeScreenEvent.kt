package org.simple.clinic.home

import org.simple.clinic.widgets.UiEvent

sealed class HomeScreenEvent : UiEvent

object HomeFacilitySelectionClicked : HomeScreenEvent() {
  override val analyticsName = "Home Screen:Facility Clicked"
}
