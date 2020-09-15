package org.simple.clinic.teleconsultlog.prescription.patientinfo

import org.simple.clinic.patient.PatientProfile
import java.time.LocalDate
import java.util.UUID

data class TeleconsultPatientInfoModel(
    val patientUuid: UUID,
    val prescriptionDate: LocalDate,
    val patientProfile: PatientProfile?,
) {

  companion object {

    fun create(patientUuid: UUID, prescriptionDate: LocalDate) = TeleconsultPatientInfoModel(
        patientUuid = patientUuid,
        prescriptionDate = prescriptionDate,
        patientProfile = null
    )
  }

  fun patientProfileLoaded(patientProfile: PatientProfile): TeleconsultPatientInfoModel {
    return copy(patientProfile = patientProfile)
  }
}
