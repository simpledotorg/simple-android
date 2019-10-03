package org.simple.clinic.newentry

import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility

interface PatientEntryUi {
  fun preFillFields(entry: OngoingNewPatientEntry)
  fun openMedicalHistoryEntryScreen()
  fun setDateOfBirthAndAgeVisibility(visibility: DateOfBirthAndAgeVisibility)
  fun setShowDatePatternInDateOfBirthLabel(showPattern: Boolean)
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
  fun scrollToFirstFieldWithError()
  fun scrollFormToBottom()
  fun showIdentifierSection()
  fun hideIdentifierSection()
}
