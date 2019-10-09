package org.simple.clinic.bp.entry

import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.LocalDate

sealed class BloodPressureEntryEvent : UiEvent

data class SheetCreated(val openAs: OpenAs) : BloodPressureEntryEvent()

data class SystolicChanged(val systolic: String) : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Systolic Text Changed"
}

data class DiastolicChanged(val diastolic: String) : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Diastolic Text Changed"
}

object SaveClicked : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Save Clicked"
}

object RemoveClicked : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Remove Clicked"
}

object DiastolicBackspaceClicked : BloodPressureEntryEvent()

data class BloodPressureReadingsValidated(val result: BpValidator.Validation) : BloodPressureEntryEvent()

data class DateValidated(val date: String, val result: UserInputDateValidator.Result) : BloodPressureEntryEvent()

data class ScreenChanged(val type: ScreenType) : BloodPressureEntryEvent()

data class DayChanged(val day: String) : BloodPressureEntryEvent()

data class MonthChanged(val month: String) : BloodPressureEntryEvent()

data class YearChanged(val twoDigitYear: String) : BloodPressureEntryEvent()

data class DateChanged(val date: String) : BloodPressureEntryEvent()

data class DateToPrefillCalculated(val date: LocalDate) : BloodPressureEntryEvent()

data class BloodPressureSaved(val wasDateChanged: Boolean) : BloodPressureEntryEvent() {
  override val analyticsName = when {
    wasDateChanged -> "Blood Pressure Entry:BP Saved With Current Date"
    else -> "Blood Pressure Entry:BP Saved With An Older Date"
  }
}

object BloodPressureDateClicked : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Next Arrow Clicked"
}

object ShowBpClicked : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Previous Arrow Clicked"
}

object BackPressed : BloodPressureEntryEvent() {
  override val analyticsName = "Blood Pressure Entry:Hardware Back Pressed"
}
