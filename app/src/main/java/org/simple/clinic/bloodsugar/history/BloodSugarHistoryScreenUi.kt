package org.simple.clinic.bloodsugar.history

import androidx.paging.PagingData
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem
import org.simple.clinic.patient.Patient

interface BloodSugarHistoryScreenUi {
  fun showPatientInformation(patient: Patient)
  fun showBloodSugars(bloodSugars: PagingData<BloodSugarHistoryListItem>)
}
