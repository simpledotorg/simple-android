package org.simple.clinic.user

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
data class OngoingRegistrationEntry(
    val uuid: UUID? = null,
    val phoneNumber: String? = null,
    val fullName: String? = null,
    val pin: String? = null,
    val pinConfirmation: String? = null,
    val facilityId: UUID? = null
): Parcelable {

  fun withPinConfirmation(pinConfirmation: String): OngoingRegistrationEntry {
    return this.copy(pinConfirmation = pinConfirmation)
  }

  fun withPhoneNumber(number: String): OngoingRegistrationEntry {
    return copy(phoneNumber = number)
  }

  fun resetPin(): OngoingRegistrationEntry {
    return this.copy(pin = null, pinConfirmation = null)
  }

  fun withName(name: String): OngoingRegistrationEntry {
    return copy(fullName = name)
  }

  fun withPin(pin: String): OngoingRegistrationEntry {
    return copy(pin = pin)
  }
}
