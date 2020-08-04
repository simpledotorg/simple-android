package org.simple.clinic.search.results

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class PatientSearchResultsEvent : UiEvent

data class PatientSearchResultClicked(val patientUuid: UUID) : PatientSearchResultsEvent() {
  override val analyticsName = "Patient Search Results:Search Result Clicked"
}

object NewOngoingPatientEntrySaved : PatientSearchResultsEvent()
