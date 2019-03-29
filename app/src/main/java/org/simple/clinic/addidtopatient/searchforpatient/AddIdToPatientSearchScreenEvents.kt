package org.simple.clinic.addidtopatient.searchforpatient

import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.widgets.UiEvent

data class SearchQueryNameChanged(val name: String) : UiEvent {
  override val analyticsName = "Add ID to Patient Search:Search Query Changed"
}

data class SearchQueryValidated(val validationErrors: List<AddIdToPatientSearchValidationError>) : UiEvent {
  override val analyticsName = "Add ID to Patient Search:Search Query Validated"
}

object SearchClicked : UiEvent {
  override val analyticsName = "Add ID to Patient Search:Search Clicked"
}

data class ScreenCreated(val identifier: Identifier) : UiEvent
