package org.simple.clinic.bp.history

import java.util.UUID

interface BloodPressureHistoryScreenUiActions {
  fun openBloodPressureEntrySheet(patientUuid: UUID)
  fun openBloodPressureUpdateSheet(bpUuid: UUID)
}
