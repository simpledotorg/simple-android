package org.simple.clinic.bp.entry

import org.simple.clinic.widgets.UiEvent

data class BloodPressureEntrySheetCreated(val openAs: OpenAs) : UiEvent

class BloodPressureSystolicTextChanged(val systolic: String) : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Systolic Text Changed"
}

class BloodPressureDiastolicTextChanged(val diastolic: String) : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Diastolic Text Changed"
}

class BloodPressureSaveClicked : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Save Clicked"
}

object BloodPressureRemoveClicked : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Remove Clicked"
}

object BloodPressureDiastolicBackspaceClicked : UiEvent
