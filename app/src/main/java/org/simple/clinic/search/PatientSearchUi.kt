package org.simple.clinic.search

import org.simple.clinic.patient.PatientSearchCriteria
import java.util.UUID

interface PatientSearchUi: PatientSearchUiActions {
  fun openSearchResultsScreen(criteria: PatientSearchCriteria)
  fun setEmptyTextFieldErrorVisible(visible: Boolean)
  fun openPatientSummary(patientUuid: UUID)
  fun showAllPatientsInFacility()
  fun hideAllPatientsInFacility()
  fun showSearchButton()
  fun hideSearchButton()
}
