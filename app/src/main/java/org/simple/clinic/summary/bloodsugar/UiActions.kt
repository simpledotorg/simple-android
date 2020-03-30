package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import java.util.UUID

interface UiActions {
  fun showBloodSugarTypeSelector()
  fun showBloodSugarHistoryScreen(patientUuid: UUID)
  fun openBloodSugarUpdateSheet(bloodSugarMeasurementUuid: UUID, measurementType: BloodSugarMeasurementType)
  fun showAlertFacilityChangeSheet(facilityName: String)
}
