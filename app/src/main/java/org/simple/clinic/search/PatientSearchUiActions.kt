package org.simple.clinic.search

import org.simple.clinic.patient.PatientSearchCriteria

interface PatientSearchUiActions {
  fun openSearchResultsScreen(criteria: PatientSearchCriteria)
}
