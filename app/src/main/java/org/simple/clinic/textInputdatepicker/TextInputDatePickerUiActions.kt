package org.simple.clinic.textInputdatepicker

interface TextInputDatePickerUiActions {
  fun dismissSheet()
  fun hideErrorMessage()
  fun showInvalidDateError()
  fun showDateIsInPastError()
  fun showMaximumDateRangeError()
}
