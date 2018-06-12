package org.resolvetosavelives.red.search

import org.resolvetosavelives.red.patient.PatientSearchResult
import org.resolvetosavelives.red.widgets.UiEvent

// TODO: Rename to "Up".
class BackButtonClicked : UiEvent

class CreateNewPatientClicked : UiEvent

data class PatientSearchResultClicked(val searchResult: PatientSearchResult) : UiEvent
