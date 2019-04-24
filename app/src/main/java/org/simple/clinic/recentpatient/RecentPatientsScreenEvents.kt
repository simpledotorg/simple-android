package org.simple.clinic.recentpatient

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class RecentPatientItemClicked(val patientUuid: UUID) : UiEvent {
  override val analyticsName = "Recent Patients: Item clicked"
}
