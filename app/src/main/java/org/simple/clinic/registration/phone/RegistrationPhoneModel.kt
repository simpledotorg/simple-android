package org.simple.clinic.registration.phone

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationPhoneModel(
    val mode: RegistrationUiMode,
    val ongoingRegistrationEntry: OngoingRegistrationEntry?,
    val registrationResult: RegistrationResult?,
    val phoneValidationResult: RegistrationPhoneValidationResult?
) : Parcelable {

  companion object {

    fun create(): RegistrationPhoneModel = RegistrationPhoneModel(
        mode = RegistrationUiMode.PhoneEntry,
        ongoingRegistrationEntry = null,
        registrationResult = null,
        phoneValidationResult = null
    )
  }

  val isEnteredNumberValid: Boolean
    get() = phoneValidationResult != null && phoneValidationResult == RegistrationPhoneValidationResult.Valid

  fun phoneNumberChanged(phoneNumber: String): RegistrationPhoneModel {
    // TODO (vs) 04/06/20: Change in a later commit to not require a null check
    return if (ongoingRegistrationEntry != null) {
      copy(
          ongoingRegistrationEntry = ongoingRegistrationEntry.withPhoneNumber(phoneNumber),
          phoneValidationResult = null,
          registrationResult = null
      )
    } else {
      this
    }
  }

  fun withEntry(entry: OngoingRegistrationEntry): RegistrationPhoneModel {
    return copy(ongoingRegistrationEntry = entry)
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
