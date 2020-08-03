package org.simple.clinic.search

import java.util.UUID

interface PatientSearchUi: PatientSearchUiActions {
  fun setEmptyTextFieldErrorVisible(visible: Boolean)
  fun openPatientSummary(patientUuid: UUID)
  fun showAllPatientsInFacility()
  fun hideAllPatientsInFacility()
  fun showSearchButton()
  fun hideSearchButton()
}
