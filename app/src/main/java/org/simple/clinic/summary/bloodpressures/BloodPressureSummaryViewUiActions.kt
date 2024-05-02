package org.simple.clinic.summary.bloodpressures

import org.simple.clinic.facility.Facility
import java.util.UUID

interface BloodPressureSummaryViewUiActions {
  fun openBloodPressureEntrySheet(patientUuid: UUID, currentFacility: Facility)
  fun openBloodPressureUpdateSheet(bpUuid: UUID)
  fun showBloodPressureHistoryScreen(patientUuid: UUID)
}
