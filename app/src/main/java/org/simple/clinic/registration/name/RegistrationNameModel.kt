package org.simple.clinic.registration.name

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationNameModel(
    val ongoingRegistrationEntry: OngoingRegistrationEntry
) : Parcelable {

  companion object {
    fun create(
        registrationEntry: OngoingRegistrationEntry
    ): RegistrationNameModel = RegistrationNameModel(ongoingRegistrationEntry = registrationEntry)
  }
}
