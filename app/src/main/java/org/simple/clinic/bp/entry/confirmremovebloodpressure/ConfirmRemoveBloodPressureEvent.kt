package org.simple.clinic.bp.entry.confirmremovebloodpressure

import org.simple.clinic.widgets.UiEvent

sealed class ConfirmRemoveBloodPressureEvent : UiEvent

data object BloodPressureDeleted : ConfirmRemoveBloodPressureEvent()

data object ConfirmRemoveBloodPressureDialogRemoveClicked : ConfirmRemoveBloodPressureEvent() {
  override val analyticsName = "Confirm Remove Blood Pressure:Remove Clicked"
}
