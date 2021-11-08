package org.simple.clinic.newentry

import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.patient.OngoingNewPatientEntry

interface PatientEntryUiActions {
  fun prefillFields(entry: OngoingNewPatientEntry)
  fun scrollToFirstFieldWithError()
  fun scrollFormOnGenderSelection()
  fun setShowDatePatternInDateOfBirthLabel(showPattern: Boolean)
  fun openMedicalHistoryEntryScreen()
  fun setupUi(inputFields: InputFields)
}
