package org.simple.clinic.enterotp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.user.User

@Parcelize
data class EnterOtpModel(
    val user: User?,
    val otpValidationResult: ValidationResult,
    val asyncOpError: AsyncOpError?,
    val isAsyncOperationOngoing: Boolean
) : Parcelable {

  companion object {

    fun create(): EnterOtpModel {
      return EnterOtpModel(
          user = null,
          otpValidationResult = ValidationResult.NotValidated,
          asyncOpError = null,
          isAsyncOperationOngoing = false
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

  fun loginStarted(): EnterOtpModel {
    return copy(isAsyncOperationOngoing = true, asyncOpError = null)
  }

  fun loginFinished(): EnterOtpModel {
    return copy(isAsyncOperationOngoing = false, asyncOpError = null)
  }

  fun loginFailed(error: AsyncOpError): EnterOtpModel {
    return copy(asyncOpError = error)
  }

  fun requestLoginOtpStarted(): EnterOtpModel {
    return copy(asyncOpError = null, isAsyncOperationOngoing = true)
  }

  fun requestLoginOtpFinished(): EnterOtpModel {
    return copy(isAsyncOperationOngoing = false, asyncOpError = null)
  }

  fun requestLoginOtpFailed(error: AsyncOpError): EnterOtpModel {
    return copy(asyncOpError = error)
  }
}
