package org.simple.clinic.summary.updatephone

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.PatientUuid

@Parcelize
data class UpdatePhoneNumberModel(
    val patientUuid: PatientUuid
) : Parcelable {

  companion object {
    fun create(patientUuid: PatientUuid) = UpdatePhoneNumberModel(
        patientUuid = patientUuid
    )
  }
}
