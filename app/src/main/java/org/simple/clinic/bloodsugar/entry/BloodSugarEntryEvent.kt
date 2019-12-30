package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet.ScreenType
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate

sealed class BloodSugarEntryEvent : UiEvent

data class BloodSugarChanged(
    val bloodSugarReading: String
) : BloodSugarEntryEvent() {
  override val analyticsName: String = "Blood Sugar Entry:Blood Sugar Text Changed"
}

data class DayChanged(
    val day: String
) : BloodSugarEntryEvent()

data class MonthChanged(
    val month: String
) : BloodSugarEntryEvent()

data class YearChanged(
    val twoDigitYear: String
) : BloodSugarEntryEvent()

data class ScreenChanged(val type: ScreenType) : BloodSugarEntryEvent()

data class BloodSugarSaved(val wasDateChanged: Boolean) : BloodSugarEntryEvent() {
  override val analyticsName: String = when {
    wasDateChanged -> "Blood Sugar Entry:Blood Sugar Saved With Current Date"
    else -> "Blood Sugar Entry:Blood Sugar With An Older Date"
  }
}

data class DatePrefilled(val prefilledDate: LocalDate) : BloodSugarEntryEvent()

object BackPressed : BloodSugarEntryEvent()

object BloodSugarDateClicked : BloodSugarEntryEvent()

object ShowBloodSugarEntryClicked : BloodSugarEntryEvent()

object SaveClicked : BloodSugarEntryEvent() {
  override val analyticsName: String = "Blood Sugar Entry:Save Clicked"
}
