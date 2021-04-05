package org.simple.clinic.textInputdatepicker

import java.time.LocalDate

interface TextInputDatePickerUiActions {
  fun dismissSheet()
  fun hideErrorMessage()
  fun showInvalidDateError()
  fun showDateIsInPastError()
  fun showMaximumDateRangeError()
  fun userEnteredDateSelected(userEnteredDate: LocalDate)
}
