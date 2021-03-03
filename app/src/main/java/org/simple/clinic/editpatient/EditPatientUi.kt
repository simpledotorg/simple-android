package org.simple.clinic.editpatient

import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.patient.Gender
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import java.time.LocalDate

interface EditPatientUi {
  fun displayBpPassports(identifiers: List<String>)
  fun setPatientName(name: String)
  fun setPatientPhoneNumber(number: String)
  fun setColonyOrVillage(colonyOrVillage: String)
  fun setDistrict(district: String)
  fun setStreetAddress(streetAddress: String?)
  fun setZone(zone: String?)
  fun setState(state: String)
  fun setGender(gender: Gender)
  fun setPatientAge(age: Int)
  fun setPatientDateOfBirth(dateOfBirth: LocalDate)
  fun setBangladeshNationalId(nationalId: String)
  fun showValidationErrors(errors: Set<EditPatientValidationError>)
  fun hideValidationErrors(errors: Set<EditPatientValidationError>)
  fun scrollToFirstFieldWithError()
  fun goBack()
  fun showDatePatternInDateOfBirthLabel()
  fun hideDatePatternInDateOfBirthLabel()
  fun setDateOfBirthAndAgeVisibility(visibility: DateOfBirthAndAgeVisibility)
  fun showDiscardChangesAlert()
  fun showProgress()
  fun hideProgress()
  fun setupUi(inputFields: InputFields)
  fun setColonyOrVillagesAutoComplete(colonyOrVillageList: List<String>)
}
