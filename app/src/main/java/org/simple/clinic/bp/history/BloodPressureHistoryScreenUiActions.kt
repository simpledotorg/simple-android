package org.simple.clinic.bp.history

import org.simple.clinic.bp.BloodPressureHistoryListItemDataSourceFactory
import java.util.UUID

interface BloodPressureHistoryScreenUiActions {
  fun openBloodPressureEntrySheet(patientUuid: UUID)
  fun openBloodPressureUpdateSheet(bpUuid: UUID)
  fun showBloodPressures(dataSourceFactory: BloodPressureHistoryListItemDataSourceFactory)
}
