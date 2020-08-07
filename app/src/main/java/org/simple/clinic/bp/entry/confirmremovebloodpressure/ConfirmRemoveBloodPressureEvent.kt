package org.simple.clinic.bp.entry.confirmremovebloodpressure

import org.simple.clinic.widgets.UiEvent

sealed class ConfirmRemoveBloodPressureEvent : UiEvent

object BloodPressureDeleted : ConfirmRemoveBloodPressureEvent()

object ConfirmRemoveBloodPressureDialogRemoveClicked : ConfirmRemoveBloodPressureEvent() {
  override val analyticsName = "Confirm Remove Blood Pressure:Remove Clicked"
}
