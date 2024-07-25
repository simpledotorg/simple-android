package org.simple.clinic.bp.history

import androidx.paging.PagingData
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem
import org.simple.clinic.patient.Patient

interface BloodPressureHistoryScreenUi {
  fun showPatientInformation(patient: Patient)
  fun showBloodPressures(bloodPressures: PagingData<BloodPressureHistoryListItem>)
}
