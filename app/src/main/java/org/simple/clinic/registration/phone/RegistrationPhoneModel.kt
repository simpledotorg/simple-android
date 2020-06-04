package org.simple.clinic.registration.phone

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationPhoneModel(
    private val ongoingRegistrationEntry: OngoingRegistrationEntry?
) : Parcelable {

  companion object {

    fun create(): RegistrationPhoneModel = RegistrationPhoneModel(
        ongoingRegistrationEntry = null
    )
  }

  fun phoneNumberChanged(phoneNumber: String): RegistrationPhoneModel {
    // TODO (vs) 04/06/20: Change in a later commit to not require a null check
    return if (ongoingRegistrationEntry != null)
      copy(ongoingRegistrationEntry = ongoingRegistrationEntry.withPhoneNumber(phoneNumber))
    else
      this
  }
}
