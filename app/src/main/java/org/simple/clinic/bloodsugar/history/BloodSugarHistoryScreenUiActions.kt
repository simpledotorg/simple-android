package org.simple.clinic.bloodsugar.history

import java.util.UUID

interface BloodSugarHistoryScreenUiActions {
  fun openBloodSugarEntrySheet(patientUuid: UUID)
}
