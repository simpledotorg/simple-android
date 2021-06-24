package org.simple.clinic.recentpatient

import androidx.paging.PagingData
import org.simple.clinic.patient.RecentPatient
import java.util.UUID

interface AllRecentPatientsUiActions {
  fun openPatientSummary(patientUuid: UUID)
  fun showRecentPatients(recentPatients: PagingData<RecentPatient>)
}
