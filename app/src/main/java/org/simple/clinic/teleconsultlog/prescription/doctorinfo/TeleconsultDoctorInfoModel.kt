package org.simple.clinic.teleconsultlog.prescription.doctorinfo

data class TeleconsultDoctorInfoModel(
    val medicalRegistrationId: String?
) {

  companion object {

    fun create() = TeleconsultDoctorInfoModel(
        medicalRegistrationId = null
    )
  }

  val hasMedicalRegistrationId: Boolean
    get() = medicalRegistrationId != null

  fun medicalRegistrationIdLoaded(medicalRegistrationId: String): TeleconsultDoctorInfoModel {
    return copy(medicalRegistrationId = medicalRegistrationId)
  }
}
