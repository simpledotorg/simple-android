package org.simple.clinic.summary.addphone

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.registration.phone.PhoneNumberValidator

@Parcelize
data class AddPhoneNumberModel(
    val patientUuid: PatientUuid,
    val validationResult: PhoneNumberValidator.Result?
) : Parcelable {

  fun validatedPhoneNumber(validationResult: PhoneNumberValidator.Result): AddPhoneNumberModel {
    return copy(validationResult = validationResult)
  }

  companion object {
    fun create(patientUuid: PatientUuid) = AddPhoneNumberModel(
        patientUuid = patientUuid,
        validationResult = null
    )
  }

  val hasValidationResult: Boolean
    get() = validationResult != null
}
