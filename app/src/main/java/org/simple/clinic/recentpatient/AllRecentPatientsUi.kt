package org.simple.clinic.recentpatient

import org.simple.clinic.recentpatientsview.RecentPatientItemType
import java.util.UUID

interface AllRecentPatientsUi : AllRecentPatientsUiActions {
  fun openPatientSummary(patientUuid: UUID)
  fun updateRecentPatients(allItemTypes: List<RecentPatientItemType>)
}
