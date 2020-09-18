package org.simple.clinic.teleconsultlog.prescription.doctorinfo

data class TeleconsultDoctorInfoModel(
    private val medicalRegistrationId: String?
) {

  companion object {

    fun create() = TeleconsultDoctorInfoModel(
        medicalRegistrationId = null
    )
  }

  fun medicalRegistrationIdLoaded(medicalRegistrationId: String): TeleconsultDoctorInfoModel {
    return copy(medicalRegistrationId = medicalRegistrationId)
  }
}
