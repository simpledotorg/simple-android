package org.simple.clinic.addidtopatient.searchforpatient

import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class SearchQueryTextChanged(val text: String) : UiEvent {
  override val analyticsName = "Add ID to Patient Search:Search Query Changed"
}

sealed class SearchQueryValidationResult : UiEvent {

  data class Valid(val text: String) : SearchQueryValidationResult() {
    override val analyticsName: String = "Add ID to Patient Search:Search Query Validated:Valid"
  }

  data class Invalid(val validationErrors: List<AddIdToPatientSearchValidationError>) : SearchQueryValidationResult() {
    override val analyticsName = "Add ID to Patient Search:Search Query Validated:Invalid"
  }
}

object SearchClicked : UiEvent {
  override val analyticsName = "Add ID to Patient Search:Search Clicked"
}

data class ScreenCreated(val identifier: Identifier) : UiEvent

data class PatientItemClicked(val patientUuid: UUID) : UiEvent
