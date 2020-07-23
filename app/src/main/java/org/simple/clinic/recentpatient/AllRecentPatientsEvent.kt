package org.simple.clinic.recentpatient

import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class AllRecentPatientsEvent : UiEvent

data class RecentPatientsLoaded(val recentPatients: List<RecentPatient>) : AllRecentPatientsEvent()

data class RecentPatientItemClicked(val patientUuid: UUID) : AllRecentPatientsEvent() {
  override val analyticsName = "Recent Patients: Item clicked"
}
