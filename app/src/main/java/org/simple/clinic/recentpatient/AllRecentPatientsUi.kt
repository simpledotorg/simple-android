package org.simple.clinic.recentpatient

import java.util.UUID

interface AllRecentPatientsUi {
  fun openPatientSummary(patientUuid: UUID)
  fun updateRecentPatients(allItemTypes: List<RecentPatientItem>)
}
