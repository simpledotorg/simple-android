package org.simple.clinic.registration.name

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class RegistrationNameModel : Parcelable {

  companion object {
    fun create(): RegistrationNameModel = RegistrationNameModel()
  }
}
