package org.simple.clinic.bp.entry

import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType
import org.simple.clinic.widgets.UiEvent

data class BloodPressureEntrySheetCreated(val openAs: OpenAs) : UiEvent

data class BloodPressureSystolicTextChanged(val systolic: String) : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Systolic Text Changed"
}

data class BloodPressureDiastolicTextChanged(val diastolic: String) : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Diastolic Text Changed"
}

object BloodPressureSaveClicked : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Save Clicked"
}

object BloodPressureRemoveClicked : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Remove Clicked"
}

object BloodPressureDiastolicBackspaceClicked : UiEvent

data class BloodPressureScreenChanged(val type: ScreenType) : UiEvent
