package org.simple.clinic.registration.register

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class RegistrationLoadingModel : Parcelable {

  companion object {
    fun create(): RegistrationLoadingModel = RegistrationLoadingModel()
  }
}
