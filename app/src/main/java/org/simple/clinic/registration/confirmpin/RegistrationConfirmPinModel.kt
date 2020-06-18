package org.simple.clinic.registration.confirmpin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinValidationResult.NotValidated
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinValidationResult.Valid
import org.simple.clinic.user.OngoingRegistrationEntry
import org.threeten.bp.Instant

@Parcelize
data class RegistrationConfirmPinModel(
    val ongoingRegistrationEntry: OngoingRegistrationEntry,
    val enteredPinConfirmation: String,
    val confirmPinValidationResult: RegistrationConfirmPinValidationResult
) : Parcelable {

  companion object {
    fun create(
        registrationEntry: OngoingRegistrationEntry
    ): RegistrationConfirmPinModel {
      return RegistrationConfirmPinModel(
          ongoingRegistrationEntry = registrationEntry,
          enteredPinConfirmation = "",
          confirmPinValidationResult = NotValidated
      )
    }
  }

  val pinConfirmationMatchesEnteredPin: Boolean
    get() = confirmPinValidationResult == Valid

  fun withEnteredPinConfirmation(confirmPin: String): RegistrationConfirmPinModel {
    return copy(enteredPinConfirmation = confirmPin, confirmPinValidationResult = NotValidated)
  }

  fun validatedPinConfirmation(result: RegistrationConfirmPinValidationResult): RegistrationConfirmPinModel {
    return copy(confirmPinValidationResult = result)
  }

  fun updateRegistrationEntryWithPinConfirmation(
      pinConfirmation: String,
      timestamp: Instant
  ): RegistrationConfirmPinModel {
    return copy(ongoingRegistrationEntry = ongoingRegistrationEntry.withPinConfirmation(pinConfirmation, timestamp))
  }
}
