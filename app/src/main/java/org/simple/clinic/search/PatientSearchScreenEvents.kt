package org.simple.clinic.search

import org.simple.clinic.widgets.UiEvent

data class PatientSearchScreenCreated(val key: PatientSearchScreenKey): UiEvent

data class SearchQueryNameChanged(val name: String) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Changed"
}

data class SearchQueryDateOfBirthChanged(val dateOfBirth: String) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Date Of Birth Changed"
}

data class SearchQueryAgeChanged(val ageString: String) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Age Changed"
}

class SearchClicked : UiEvent {
  override val analyticsName = "Patient Search:Search Clicked"
}
