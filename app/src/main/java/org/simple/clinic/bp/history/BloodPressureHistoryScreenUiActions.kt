package org.simple.clinic.bp.history

import java.util.UUID

interface BloodPressureHistoryScreenUiActions {
  fun openBloodPressureEntrySheet()
  fun openBloodPressureUpdateSheet(bpUuid: UUID)
}
