package org.simple.clinic.textInputdatepicker

import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate

sealed class TextInputDatePickerEvent : UiEvent

data object DismissSheetClicked : TextInputDatePickerEvent() {
  override val analyticsName: String = "Text Input Date Picker:Dismiss sheet clicked"
}

data class DayChanged(val day: String) : TextInputDatePickerEvent()

data class MonthChanged(val month: String) : TextInputDatePickerEvent()

data class YearChanged(val year: String) : TextInputDatePickerEvent()

data object DoneClicked : TextInputDatePickerEvent() {
  override val analyticsName: String = "Text Input Date Picker:Done Clicked"
}

data class DatePrefilled(val prefilledDate: LocalDate) : TextInputDatePickerEvent()
