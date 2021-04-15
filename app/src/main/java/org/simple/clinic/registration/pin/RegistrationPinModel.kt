package org.simple.clinic.registration.pin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.registration.pin.RegistrationPinValidationResult.DoesNotMatchRequiredLength
import org.simple.clinic.registration.pin.RegistrationPinValidationResult.NotValidated
import org.simple.clinic.registration.pin.RegistrationPinValidationResult.Valid
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationPinModel(
    val ongoingRegistrationEntry: OngoingRegistrationEntry,
    val pinValidationResult: RegistrationPinValidationResult
) : Parcelable {

  companion object {
    fun create(registrationEntry: OngoingRegistrationEntry): RegistrationPinModel = RegistrationPinModel(
        ongoingRegistrationEntry = registrationEntry,
        pinValidationResult = NotValidated
    )
  }

  val isEnteredPinValid: Boolean
    get() = pinValidationResult == Valid

  fun pinChanged(pin: String): RegistrationPinModel {
    return copy(
        ongoingRegistrationEntry = ongoingRegistrationEntry.withPin(pin),
        pinValidationResult = NotValidated
    )
  }

  fun validPinEntered(): RegistrationPinModel {
    return copy(pinValidationResult = Valid)
  }

  fun pinDoesNotMatchRequiredLength(): RegistrationPinModel {
    return copy(pinValidationResult = DoesNotMatchRequiredLength)
  }

  fun isEnteredPinOfLength(length: Int): Boolean = ongoingRegistrationEntry.pin?.length == length
}
