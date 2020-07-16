package org.simple.clinic.recentpatientsview

import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class LatestRecentPatientsEvent : UiEvent

data class RecentPatientsLoaded(val recentPatients: List<RecentPatient>) : LatestRecentPatientsEvent()

data class RecentPatientItemClicked(val patientUuid: UUID) : LatestRecentPatientsEvent() {
  override val analyticsName = "Recent Patients: Item clicked"
}

object SeeAllItemClicked : LatestRecentPatientsEvent() {
  override val analyticsName = "Recent Patients: See all clicked"
}
