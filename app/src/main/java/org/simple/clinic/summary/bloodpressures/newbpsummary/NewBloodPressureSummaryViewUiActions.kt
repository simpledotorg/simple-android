package org.simple.clinic.summary.bloodpressures.newbpsummary

import java.util.UUID

interface NewBloodPressureSummaryViewUiActions {
  fun openBloodPressureEntrySheet(patientUuid: UUID)
  fun openBloodPressureUpdateSheet(bpUuid: UUID)
  fun showBloodPressureHistoryScreen(patientUuid: UUID)
}
