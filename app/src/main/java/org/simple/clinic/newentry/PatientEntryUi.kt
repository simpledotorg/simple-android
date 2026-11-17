package org.simple.clinic.newentry

import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility

interface PatientEntryUi {
  fun setDateOfBirthAndAgeVisibility(visibility: DateOfBirthAndAgeVisibility)
  fun showIdentifierSection(identifier: Identifier)
  fun hideIdentifierSection()
  fun nextButtonShowInProgress()
  fun enableNextButton()
  fun setColonyOrVillagesAutoComplete(colonyOrVillageList: List<String>)
  fun setupUi(inputFields: InputFields)
  fun displayShrId(shrId: Identifier)
  fun showAddShrIdButton()
  fun hideShrId()
}
