package org.simple.clinic.bp.entry

import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator

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

// TODO: Find a better name or revert to using hardcoded values.
data class BloodPressureDateValidated(val date: String) : UiEvent {
  fun result(validator: DateOfBirthFormatValidator) = validator.validate2(date)
}

data class BloodPressureScreenChanged(val type: ScreenType) : UiEvent

enum class ScreenType {
  BP_ENTRY,
  DATE_ENTRY
}

data class BloodPressureDayChanged(val day: String) : UiEvent
data class BloodPressureMonthChanged(val month: String) : UiEvent
data class BloodPressureYearChanged(val year: String) : UiEvent
data class BloodPressureDateChanged(val date: String): UiEvent
