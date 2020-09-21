package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import org.simple.clinic.patient.Patient
import org.simple.clinic.user.User

data class TeleconsultDoctorInfoModel(
    val medicalRegistrationId: String?,
    val instructions: String?,
    val user: User?
) {

  companion object {

    fun create() = TeleconsultDoctorInfoModel(
        medicalRegistrationId = null,
        instructions = null,
        user = null
    )
  }

  val hasMedicalRegistrationId: Boolean
    get() = medicalRegistrationId != null

  fun medicalRegistrationIdLoaded(medicalRegistrationId: String): TeleconsultDoctorInfoModel {
    return copy(medicalRegistrationId = medicalRegistrationId)
  }

  fun medicalInstructionsChanged(instructions: String): TeleconsultDoctorInfoModel {
    return copy(instructions = instructions)
  }

  fun currentUserLoaded(user: User): TeleconsultDoctorInfoModel {
    return copy(user = user)
  }
}
