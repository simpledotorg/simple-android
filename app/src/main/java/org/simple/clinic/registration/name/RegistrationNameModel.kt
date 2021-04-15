package org.simple.clinic.registration.name

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.registration.name.RegistrationNameValidationResult.NotValidated
import org.simple.clinic.registration.name.RegistrationNameValidationResult.Valid
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationNameModel(
    val ongoingRegistrationEntry: OngoingRegistrationEntry,
    val nameValidationResult: RegistrationNameValidationResult
) : Parcelable {

  companion object {
    fun create(
        registrationEntry: OngoingRegistrationEntry
    ): RegistrationNameModel {
      return RegistrationNameModel(
          ongoingRegistrationEntry = registrationEntry,
          nameValidationResult = NotValidated
      )
    }
  }

  val isEnteredNameValid: Boolean
    get() = nameValidationResult == Valid

  fun nameChanged(fullName: String): RegistrationNameModel {
    return copy(
        ongoingRegistrationEntry = ongoingRegistrationEntry.withName(fullName),
        nameValidationResult = NotValidated
    )
  }

  fun nameValidated(nameValidationResult: RegistrationNameValidationResult): RegistrationNameModel {
    return copy(nameValidationResult = nameValidationResult)
  }
}
