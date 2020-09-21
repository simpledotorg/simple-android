package org.simple.clinic.teleconsultlog.prescription.doctorinfo

data class TeleconsultDoctorInfoModel(
    val medicalRegistrationId: String?,
    val instructions: String?
) {

  companion object {

    fun create() = TeleconsultDoctorInfoModel(
        medicalRegistrationId = null,
        instructions = null
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
}
