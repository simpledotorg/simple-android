package org.simple.clinic.bloodsugar.history

import org.simple.clinic.patient.Patient
import java.util.UUID

data class BloodSugarHistoryScreenModel(
    val patientUuid: UUID,
    val patient: Patient?
) {
  companion object {
    fun create(patientUuid: UUID) = BloodSugarHistoryScreenModel(patientUuid, null)
  }

  fun patientLoaded(patient: Patient): BloodSugarHistoryScreenModel =
      copy(patient = patient)
}
