package org.simple.clinic.instantsearch

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult

interface InstantSearchUiActions {
  fun showPatientsSearchResults(patients: List<PatientSearchResult>, facility: Facility)
}
