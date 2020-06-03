package org.simple.clinic.registration.phone

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class RegistrationPhoneModel : Parcelable {

  companion object {
    fun create(): RegistrationPhoneModel = RegistrationPhoneModel()
  }
}
