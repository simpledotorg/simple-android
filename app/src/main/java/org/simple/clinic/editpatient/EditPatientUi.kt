package org.simple.clinic.editpatient

import org.simple.clinic.patient.Gender
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.threeten.bp.LocalDate

interface EditPatientUi {
  fun setPatientName(name: String)
  fun setPatientPhoneNumber(number: String)
  fun setColonyOrVillage(colonyOrVillage: String)
  fun setDistrict(district: String)
  fun setState(state: String)
  fun setGender(gender: Gender)
  fun setPatientAge(age: Int)
  fun setPatientDateOfBirth(dateOfBirth: LocalDate)
  fun showValidationErrors(errors: Set<EditPatientValidationError>)
  fun hideValidationErrors(errors: Set<EditPatientValidationError>)
  fun scrollToFirstFieldWithError()
  fun goBack()
  fun showDatePatternInDateOfBirthLabel()
  fun hideDatePatternInDateOfBirthLabel()
  fun setDateOfBirthAndAgeVisibility(visibility: DateOfBirthAndAgeVisibility)
  fun showDiscardChangesAlert()
}
