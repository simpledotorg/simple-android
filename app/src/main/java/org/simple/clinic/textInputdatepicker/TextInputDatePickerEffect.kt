package org.simple.clinic.textInputdatepicker

import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result

sealed class TextInputDatePickerEffect {

  object DismissSheet : TextInputDatePickerEffect()

  object HideDateErrorMessage : TextInputDatePickerEffect()

  data class ShowDateValidationError(val dateValidation: Result) : TextInputDatePickerEffect()
}
