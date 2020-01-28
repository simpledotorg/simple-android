package org.simple.clinic.newentry

import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility

interface PatientEntryUi {
  fun prefillFields(entry: OngoingNewPatientEntry)
  fun openMedicalHistoryEntryScreen()
  fun setDateOfBirthAndAgeVisibility(visibility: DateOfBirthAndAgeVisibility)
  fun setShowDatePatternInDateOfBirthLabel(showPattern: Boolean)
  fun scrollToFirstFieldWithError()
  fun scrollFormOnGenderSelection()
  fun showIdentifierSection()
  fun hideIdentifierSection()

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
  fun showAgeExceedsMaxLimitError(show: Boolean)
  fun showDOBExceedsMaxLimitError(show: Boolean)
  fun showAgeExceedsMinLimitError(show: Boolean)
  fun showDOBExceedsMinLimitError(show: Boolean)
}
