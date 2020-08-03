package org.simple.clinic.search

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class PatientSearchEvent: UiEvent

data class SearchQueryTextChanged(val text: String) : PatientSearchEvent() {
  override val analyticsName = "Patient Search:Search Query Changed"
}

class SearchClicked : PatientSearchEvent() {
  override val analyticsName = "Patient Search:Search Clicked"
}

data class PatientItemClicked(val patientUuid: UUID) : PatientSearchEvent() {
  override val analyticsName = "Patient Search:Patient Item Clicked"
}
