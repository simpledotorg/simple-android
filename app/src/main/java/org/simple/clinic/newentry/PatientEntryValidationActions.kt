package org.simple.clinic.newentry

interface PatientEntryValidationActions {
  fun showEmptyFullNameError(show: Boolean)
  fun showLengthTooShortPhoneNumberError(show: Boolean)
  fun showLengthTooLongPhoneNumberError(show: Boolean)
  fun showMissingGenderError(show: Boolean)
  fun showEmptyColonyOrVillageError(show: Boolean)
  fun showEmptyDistrictError(show: Boolean)
  fun showEmptyStateError(show: Boolean)
  fun showEmptyDateOfBirthAndAgeError(show: Boolean)
  fun showInvalidDateOfBirthError(show: Boolean)
  fun showDateOfBirthIsInFutureError(show: Boolean)
}
