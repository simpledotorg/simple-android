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

  val isEnteredPinInvalid: Boolean
    get() = otpValidationResult == ValidationResult.IsNotRequiredLength

  fun userLoaded(user: User): EnterOtpModel {
    return copy(user = user)
  }

  fun enteredOtpValid(): EnterOtpModel {
    return copy(otpValidationResult = ValidationResult.Valid)
  }

  fun enteredOtpNotRequiredLength(): EnterOtpModel {
    return copy(otpValidationResult = ValidationResult.IsNotRequiredLength)
  }
}
