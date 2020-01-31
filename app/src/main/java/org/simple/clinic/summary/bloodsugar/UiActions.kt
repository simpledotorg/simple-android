package org.simple.clinic.summary.bloodsugar

import java.util.UUID

interface UiActions {
  fun showBloodSugarTypeSelector()
  fun showBloodSugarHistoryScreen(patientUuid: UUID)
}
