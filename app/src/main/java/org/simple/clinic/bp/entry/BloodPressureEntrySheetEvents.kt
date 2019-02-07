package org.simple.clinic.bp.entry

import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.LocalDate

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

data class BloodPressureReadingsValidated(val result: BpValidator.Validation) : UiEvent

data class BloodPressureDateValidated(val date: String, val result: UserInputDateValidator.Result2) : UiEvent

data class BloodPressureScreenChanged(val type: ScreenType) : UiEvent

data class BloodPressureDayChanged(val day: String) : UiEvent
data class BloodPressureMonthChanged(val month: String) : UiEvent
data class BloodPressureYearChanged(val twoDigitYear: String) : UiEvent
data class BloodPressureDateChanged(val date: String) : UiEvent

data class BloodPressureDateToPrefillCalculated(val date: LocalDate) : UiEvent

data class BloodPressureSaved(val wasDateChanged: Boolean) : UiEvent {
  override val analyticsName = when {
    wasDateChanged -> "Blood Pressure Entry:BP Saved With Current Date"
    else -> "Blood Pressure Entry:BP Saved With An Older Date"
  }
}

object BloodPressureNextArrowClicked : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Next Arrow Clicked"
}

object BloodPressurePreviousArrowClicked : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Previous Arrow Clicked"
}

object BloodPressureBackPressed : UiEvent {
  override val analyticsName = "Blood Pressure Entry:Hardware Back Pressed"
}
