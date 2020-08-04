package org.simple.clinic.search.results

import org.simple.clinic.facility.Facility

interface PatientSearchResultsUi: PatientSearchResultsUiActions {
  fun openPatientEntryScreen(facility: Facility)
}
