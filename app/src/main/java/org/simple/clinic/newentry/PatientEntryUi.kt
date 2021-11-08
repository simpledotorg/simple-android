package org.simple.clinic.newentry

import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility

interface PatientEntryUi : PatientEntryUiActions {
  fun setDateOfBirthAndAgeVisibility(visibility: DateOfBirthAndAgeVisibility)
  fun showIdentifierSection()
  fun hideIdentifierSection()
  fun nextButtonShowInProgress()
  fun enableNextButton()
  fun setColonyOrVillagesAutoComplete(colonyOrVillageList: List<String>)
}
