package org.simple.clinic.registration.register

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RegistrationLoadingModel(
    val registerUserResult: RegisterUserResult?
) : Parcelable {

  companion object {
    fun create(): RegistrationLoadingModel {
      return RegistrationLoadingModel(registerUserResult = null)
    }
  }
}
