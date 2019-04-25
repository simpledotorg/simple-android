package org.simple.clinic.recentpatientsview

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class RecentPatientItemClicked(val patientUuid: UUID) : UiEvent {
  override val analyticsName = "Recent Patients: Item clicked"
}

object SeeAllItemClicked : UiEvent {
  override val analyticsName = "Recent Patients: See all clicked"
}
