package org.simple.clinic.recentpatientsview

import java.util.UUID

interface LatestRecentPatientsUiActions {
  fun openPatientSummary(patientUuid: UUID)
  fun openRecentPatientsScreen()
}
