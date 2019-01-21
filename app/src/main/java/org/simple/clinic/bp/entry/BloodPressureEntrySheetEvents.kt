package org.simple.clinic.bp.entry

import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator

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

data class BloodPressureBpValidated(val result: BpValidator.Validation) : UiEvent

data class BloodPressureDateValidated(val date: String) : UiEvent {
  fun result(validator: UserInputDateValidator) = validator.validate2(date)
}

data class BloodPressureScreenChanged(val type: ScreenType) : UiEvent

data class BloodPressureDayChanged(val day: String) : UiEvent
data class BloodPressureMonthChanged(val month: String) : UiEvent
data class BloodPressureYearChanged(val year: String) : UiEvent
data class BloodPressureDateChanged(val date: String) : UiEvent

object BloodPressureNextArrowClicked : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Next Arrow Clicked"
}

object BloodPressurePreviousArrowClicked : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Previous Arrow Clicked"
}

object BloodPressureBackPressed : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Hardware Back Pressed"
}
