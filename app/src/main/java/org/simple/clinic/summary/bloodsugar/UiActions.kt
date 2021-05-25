package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.facility.Facility
import java.util.UUID

interface UiActions {
  fun showBloodSugarTypeSelector(currentFacility: Facility)
  fun showBloodSugarHistoryScreen(patientUuid: UUID)
  fun openBloodSugarUpdateSheet(
      bloodSugarMeasurementUuid: UUID,
      measurementType: BloodSugarMeasurementType
  )
}
