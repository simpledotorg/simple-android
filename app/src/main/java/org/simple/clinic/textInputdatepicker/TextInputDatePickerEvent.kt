package org.simple.clinic.textInputdatepicker

import org.simple.clinic.widgets.UiEvent

sealed class TextInputDatePickerEvent : UiEvent

object DismissSheetClicked : TextInputDatePickerEvent() {
  override val analyticsName: String = "Text Input Date Picker:Dismiss sheet clicked"
}

data class DayChanged(val day: String) : TextInputDatePickerEvent()

data class MonthChanged(val month: String): TextInputDatePickerEvent()
