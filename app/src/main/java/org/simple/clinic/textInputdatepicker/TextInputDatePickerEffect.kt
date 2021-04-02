package org.simple.clinic.textInputdatepicker

sealed class TextInputDatePickerEffect {

  object DismissSheet : TextInputDatePickerEffect()

  object HideDateErrorMessage : TextInputDatePickerEffect()
}
