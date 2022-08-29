package org.simple.clinic.enterotp

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.enterotp.BruteForceOtpEntryProtection.ProtectedState
import org.simple.clinic.enterotp.OtpEntryMode.OtpEntry
import org.simple.clinic.user.User

@Parcelize
data class EnterOtpModel(
    val user: User?,
    val otpValidationResult: ValidationResult,
    val asyncOpError: AsyncOpError?,
    val isAsyncOperationOngoing: Boolean,
    val protectedState: ProtectedState,

    val minOtpRetries: Int,
    val maxOtpEntriesAllowed: Int
) : Parcelable {

  companion object {

    fun create(
        minOtpRetries: Int,
        maxOtpEntriesAllowed: Int
    ): EnterOtpModel {
      return EnterOtpModel(
          user = null,
          otpValidationResult = ValidationResult.NotValidated,
          asyncOpError = null,
          isAsyncOperationOngoing = false,
          protectedState = ProtectedState.Allowed(0, maxOtpEntriesAllowed),
          minOtpRetries = minOtpRetries,
          maxOtpEntriesAllowed = maxOtpEntriesAllowed
      )
    }
  }

  val hasNoIncorrectPinEntries: Boolean
    get() = (protectedState as ProtectedState.Allowed).attemptsMade == 0

  val hasReachedMinPinRetries: Boolean
    get() = (protectedState as ProtectedState.Allowed).attemptsMade >= minOtpRetries

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

  fun requestLoginOtpStarted(): EnterOtpModel {
    return copy(asyncOpError = null, isAsyncOperationOngoing = true)
  }

  fun requestLoginOtpFinished(): EnterOtpModel {
    return copy(isAsyncOperationOngoing = false, asyncOpError = null)
  }

  fun requestLoginOtpFailed(error: AsyncOpError): EnterOtpModel {
    return copy(asyncOpError = error)
  }

  fun setOtpEntryMode(protectedState: ProtectedState): EnterOtpModel =
      copy(protectedState = protectedState)
}
