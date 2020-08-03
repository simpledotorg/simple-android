package org.simple.clinic.search

interface PatientSearchUi {
  fun setEmptyTextFieldErrorVisible(visible: Boolean)
  fun showAllPatientsInFacility()
  fun hideAllPatientsInFacility()
  fun showSearchButton()
  fun hideSearchButton()
}
