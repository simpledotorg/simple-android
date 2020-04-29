package org.simple.clinic.bloodsugar.history

import org.simple.clinic.bloodsugar.BloodSugarHistoryListItemDataSourceFactory
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import java.util.UUID

interface BloodSugarHistoryScreenUiActions {
  fun openBloodSugarEntrySheet(patientUuid: UUID)
  fun openBloodSugarUpdateSheet(measurement: BloodSugarMeasurement)
  fun showBloodSugars(dataSourceFactory: BloodSugarHistoryListItemDataSourceFactory)
}
