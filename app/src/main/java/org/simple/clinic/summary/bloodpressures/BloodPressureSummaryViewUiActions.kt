package org.simple.clinic.summary.bloodpressures

import java.util.UUID

interface BloodPressureSummaryViewUiActions {
  fun openBloodPressureEntrySheet(patientUuid: UUID)
  fun openBloodPressureUpdateSheet(bpUuid: UUID)
  fun showBloodPressureHistoryScreen(patientUuid: UUID)
}
