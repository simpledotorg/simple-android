package org.simple.clinic.teleconsultlog.success

import org.simple.clinic.patient.Patient
import java.util.UUID

data class TeleConsultSuccessModel(
    val patientUuid: UUID,
    val patient: Patient?
) {

  companion object {
    fun create(patientUuid: UUID) = TeleConsultSuccessModel(patientUuid, null)
  }

  fun patientDetailLoaded(patient: Patient): TeleConsultSuccessModel =
      copy(patient = patient)
}
