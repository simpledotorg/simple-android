package org.simple.clinic.registration.pin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationPinModel(
    val ongoingRegistrationEntry: OngoingRegistrationEntry
) : Parcelable {

  companion object {
    fun create(registrationEntry: OngoingRegistrationEntry): RegistrationPinModel = RegistrationPinModel(registrationEntry)
  }

  fun pinChanged(pin: String): RegistrationPinModel {
    return copy(ongoingRegistrationEntry = ongoingRegistrationEntry.withPin(pin))
  }
}
