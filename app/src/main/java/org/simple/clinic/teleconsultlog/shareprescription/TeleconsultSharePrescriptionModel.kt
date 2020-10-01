package org.simple.clinic.teleconsultlog.shareprescription

import org.simple.clinic.patient.Patient
import java.time.LocalDate
import java.util.UUID

data class TeleconsultSharePrescriptionModel(
    val patientUuid: UUID,
    val patient: Patient?,
    val prescriptionDate: LocalDate
) {

  companion object {
    fun create(patientUuid: UUID, prescriptionDate: LocalDate) = TeleconsultSharePrescriptionModel(
        patientUuid = patientUuid,
        patient = null,
        prescriptionDate = prescriptionDate
    )
  }

  val hasPatient: Boolean
    get() = patient != null

  fun patientLoaded(patient: Patient?): TeleconsultSharePrescriptionModel {
    return copy(patient = patient)
  }
}
