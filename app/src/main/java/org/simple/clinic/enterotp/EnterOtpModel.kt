package org.simple.clinic.enterotp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.User

@Parcelize
data class EnterOtpModel(
    val user: User?,
    val otpValidationResult: ValidationResult
) : Parcelable {

  companion object {

    fun create(): EnterOtpModel {
      return EnterOtpModel(
          user = null,
          otpValidationResult = ValidationResult.NotValidated
      )
    }
  }

  val hasLoadedUser: Boolean
    get() = user != null

  fun userLoaded(user: User): EnterOtpModel {
    return copy(user = user)
  }
}
