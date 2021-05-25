package org.simple.clinic.newentry

interface PatientEntryValidationActions {
  fun showEmptyFullNameError(show: Boolean)
  fun showLengthTooShortPhoneNumberError(show: Boolean, requiredNumberLength: Int)
  fun showLengthTooLongPhoneNumberError(show: Boolean, requiredNumberLength: Int)
  fun showMissingGenderError(show: Boolean)
  fun showEmptyColonyOrVillageError(show: Boolean)
  fun showEmptyDistrictError(show: Boolean)
  fun showEmptyStateError(show: Boolean)
  fun showEmptyDateOfBirthAndAgeError(show: Boolean)
  fun showInvalidDateOfBirthError(show: Boolean)
  fun showDateOfBirthIsInFutureError(show: Boolean)
  fun showAgeExceedsMaxLimitError(show: Boolean)
  fun showDOBExceedsMaxLimitError(show: Boolean)
  fun showAgeExceedsMinLimitError(show: Boolean)
  fun showDOBExceedsMinLimitError(show: Boolean)
}
