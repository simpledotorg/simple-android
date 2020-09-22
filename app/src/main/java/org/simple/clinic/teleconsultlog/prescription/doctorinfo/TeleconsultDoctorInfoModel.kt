package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.User

@Parcelize
data class TeleconsultDoctorInfoModel(
    val medicalRegistrationId: String?,
    val instructions: String?,
    val user: User?
) : Parcelable {

  companion object {

    fun create() = TeleconsultDoctorInfoModel(
        medicalRegistrationId = null,
        instructions = null,
        user = null
    )
  }

  val hasMedicalRegistrationId: Boolean
    get() = medicalRegistrationId != null

  val hasUser: Boolean
    get() = user != null

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
