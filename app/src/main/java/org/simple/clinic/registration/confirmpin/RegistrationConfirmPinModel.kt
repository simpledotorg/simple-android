package org.simple.clinic.registration.confirmpin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationConfirmPinModel(
    val ongoingRegistrationEntry: OngoingRegistrationEntry,
    val enteredPinConfirmation: String
) : Parcelable {

  companion object {
    fun create(
        registrationEntry: OngoingRegistrationEntry
    ): RegistrationConfirmPinModel {
      return RegistrationConfirmPinModel(
          ongoingRegistrationEntry = registrationEntry,
          enteredPinConfirmation = ""
      )
    }
  }

  fun withEnteredPinConfirmation(confirmPin: String): RegistrationConfirmPinModel {
    return copy(enteredPinConfirmation = confirmPin)
  }
}
