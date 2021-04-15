package org.simple.clinic.registration.phone

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationPhoneModel(
    val mode: RegistrationUiMode,
    val ongoingRegistrationEntry: OngoingRegistrationEntry,
    val registrationResult: RegistrationResult?,
    val phoneValidationResult: RegistrationPhoneValidationResult?
) : Parcelable {

  companion object {

    fun create(
        registrationEntry: OngoingRegistrationEntry
    ): RegistrationPhoneModel = RegistrationPhoneModel(
        mode = RegistrationUiMode.PhoneEntry,
        ongoingRegistrationEntry = registrationEntry,
        registrationResult = null,
        phoneValidationResult = null
    )
  }

  val isEnteredNumberValid: Boolean
    get() = phoneValidationResult != null && phoneValidationResult == RegistrationPhoneValidationResult.Valid

  fun phoneNumberChanged(phoneNumber: String): RegistrationPhoneModel {
    return copy(
        ongoingRegistrationEntry = ongoingRegistrationEntry.withPhoneNumber(phoneNumber),
        phoneValidationResult = null,
        registrationResult = null
    )
  }

  fun phoneNumberValidated(result: RegistrationPhoneValidationResult): RegistrationPhoneModel {
    return copy(phoneValidationResult = result)
  }

  fun withRegistrationResult(registrationResult: RegistrationResult?): RegistrationPhoneModel {
    return copy(registrationResult = registrationResult)
  }

  fun switchToProgressMode(): RegistrationPhoneModel {
    return copy(mode = RegistrationUiMode.RegistrationOngoing)
  }

  fun switchToPhoneEntryMode(): RegistrationPhoneModel {
    return copy(mode = RegistrationUiMode.PhoneEntry)
  }

  fun clearPhoneRegistrationResult(): RegistrationPhoneModel {
    return copy(registrationResult = null)
  }
}
