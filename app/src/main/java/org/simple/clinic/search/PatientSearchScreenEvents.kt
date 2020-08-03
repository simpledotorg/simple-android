package org.simple.clinic.search

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class PatientItemClicked(val patientUuid: UUID) : UiEvent {
  override val analyticsName = "Patient Search:Patient Item Clicked"
}
