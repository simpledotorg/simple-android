package org.simple.clinic.bp.entry

import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.LocalDate

sealed class BloodPressureEntryEvent : UiEvent

data class BloodPressureEntrySheetCreated(val openAs: OpenAs) : BloodPressureEntryEvent()

data class BloodPressureSystolicTextChanged(val systolic: String) : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Systolic Text Changed"
}

data class BloodPressureDiastolicTextChanged(val diastolic: String) : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Diastolic Text Changed"
}

object BloodPressureSaveClicked : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Save Clicked"
}

object BloodPressureRemoveClicked : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Remove Clicked"
}

object BloodPressureDiastolicBackspaceClicked : BloodPressureEntryEvent()

data class BloodPressureReadingsValidated(val result: BpValidator.Validation) : BloodPressureEntryEvent()

data class BloodPressureDateValidated(val date: String, val result: UserInputDateValidator.Result) : BloodPressureEntryEvent()

data class BloodPressureScreenChanged(val type: ScreenType) : BloodPressureEntryEvent()

data class BloodPressureDayChanged(val day: String) : BloodPressureEntryEvent()

data class BloodPressureMonthChanged(val month: String) : BloodPressureEntryEvent()

data class BloodPressureYearChanged(val twoDigitYear: String) : BloodPressureEntryEvent()

data class BloodPressureDateChanged(val date: String) : BloodPressureEntryEvent()

data class BloodPressureDateToPrefillCalculated(val date: LocalDate) : BloodPressureEntryEvent()

data class BloodPressureSaved(val wasDateChanged: Boolean) : BloodPressureEntryEvent() {
  override val analyticsName = when {
    wasDateChanged -> "Blood Pressure Entry:BP Saved With Current Date"
    else -> "Blood Pressure Entry:BP Saved With An Older Date"
  }
}

object BloodPressureDateClicked : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Next Arrow Clicked"
}

object BloodPressureShowBpClicked : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Previous Arrow Clicked"
}

object BloodPressureBackPressed : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Hardware Back Pressed"
}
