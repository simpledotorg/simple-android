package org.simple.clinic.bp.entry

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class BloodPressureEntrySheetCreated(val openAs: OpenAs, val uuid: UUID) : UiEvent

class BloodPressureSystolicTextChanged(val systolic: String) : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Systolic Text Changed"
}

class BloodPressureDiastolicTextChanged(val diastolic: String) : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Diastolic Text Changed"
}

class BloodPressureSaveClicked : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Save Clicked"
}
