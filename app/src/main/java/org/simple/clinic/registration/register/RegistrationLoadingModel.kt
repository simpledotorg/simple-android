package org.simple.clinic.registration.register

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationLoadingModel(
    val registrationEntry: OngoingRegistrationEntry,
    val registerUserResult: RegisterUserResult?
) : Parcelable {

  companion object {
    fun create(registrationEntry: OngoingRegistrationEntry): RegistrationLoadingModel {
      return RegistrationLoadingModel(
          registrationEntry = registrationEntry,
          registerUserResult = null
      )
    }
  }

  val hasUserRegistrationCompleted: Boolean
    get() = registerUserResult != null

  fun withRegistrationResult(result: RegisterUserResult): RegistrationLoadingModel {
    return copy(registerUserResult = result)
  }

  fun clearRegistrationResult(): RegistrationLoadingModel {
    return copy(registerUserResult = null)
  }
}
