package org.simple.clinic.teleconsultlog.shareprescription

import org.simple.clinic.patient.Patient
import java.util.UUID

data class TeleconsultSharePrescriptionModel(
    val patientUuid: UUID,
    val patient: Patient?
) {

  companion object {
    fun create(patientUuid: UUID) = TeleconsultSharePrescriptionModel(
        patientUuid = patientUuid,
        patient = null
    )
  }

  fun patientLoaded(patient: Patient?): TeleconsultSharePrescriptionModel {
    return copy(patient = patient)
  }
}
