package org.simple.clinic.medicalhistory.newentry

import java.util.UUID

interface NewMedicalHistoryUi {
  fun openPatientSummaryScreen(patientUuid: UUID)
  fun setPatientName(patientName: String)
}
