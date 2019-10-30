package org.simple.clinic.searchresultsview

interface PatientSearchUi {
  fun updateSearchResults(results: PatientSearchResults)
  fun searchResultClicked(searchResultClickedEvent: SearchResultClicked)
  fun registerNewPatient(registerNewPatientEvent: RegisterNewPatient)
}
