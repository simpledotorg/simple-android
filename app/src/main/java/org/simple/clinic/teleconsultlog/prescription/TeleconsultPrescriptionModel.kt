package org.simple.clinic.teleconsultlog.prescription

import org.simple.clinic.patient.Patient
import java.util.UUID

data class TeleconsultPrescriptionModel(
    val patientUuid: UUID,
    val patient: Patient?
) {

  companion object {

    fun create(patientUuid: UUID) = TeleconsultPrescriptionModel(
        patientUuid = patientUuid,
        patient = null
    )
  }

  fun patientLoaded(patient: Patient): TeleconsultPrescriptionModel {
    return copy(patient = patient)
  }
}
