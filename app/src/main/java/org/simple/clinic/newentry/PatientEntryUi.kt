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
}
