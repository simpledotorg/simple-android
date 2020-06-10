package org.simple.clinic.registration.name

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationNameModel(
    val ongoingRegistrationEntry: OngoingRegistrationEntry,
    val nameValidationResult: RegistrationNameValidationResult?
) : Parcelable {

  companion object {
    fun create(
        registrationEntry: OngoingRegistrationEntry
    ): RegistrationNameModel {
      return RegistrationNameModel(
          ongoingRegistrationEntry = registrationEntry,
          nameValidationResult = null
      )
    }
  }

  fun nameChanged(fullName: String): RegistrationNameModel {
    return copy(ongoingRegistrationEntry = ongoingRegistrationEntry.withName(fullName))
  }
}
