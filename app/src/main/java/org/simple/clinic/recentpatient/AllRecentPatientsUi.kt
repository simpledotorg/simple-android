package org.simple.clinic.recentpatient

import java.util.UUID

interface AllRecentPatientsUi : AllRecentPatientsUiActions {
  fun openPatientSummary(patientUuid: UUID)
  fun updateRecentPatients(allItemTypes: List<RecentPatientItem>)
}
