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

  val hasUserRegistrationCompleted: Boolean
    get() = registerUserResult != null

  fun withRegistrationResult(result: RegisterUserResult): RegistrationLoadingModel {
    return copy(registerUserResult = result)
  }
}
