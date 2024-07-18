package org.simple.clinic.textInputdatepicker

import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result
import java.time.LocalDate

sealed class TextInputDatePickerEffect {

  data object DismissSheet : TextInputDatePickerEffect()

  data object HideDateErrorMessage : TextInputDatePickerEffect()

  data class ShowDateValidationError(val dateValidation: Result) : TextInputDatePickerEffect()

  data class UserEnteredDateSelected(val userEnteredDate: LocalDate) : TextInputDatePickerEffect()

  data class PrefilledDate(val date: LocalDate?) : TextInputDatePickerEffect()
}
