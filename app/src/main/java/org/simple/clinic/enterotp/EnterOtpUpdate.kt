package org.simple.clinic.enterotp

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.enterotp.BruteForceOtpEntryProtection.ProtectedState
import org.simple.clinic.enterotp.BruteForceOtpEntryProtection.ProtectedState.Allowed
import org.simple.clinic.enterotp.BruteForceOtpEntryProtection.ProtectedState.Blocked
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.activateuser.ActivateUser
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class EnterOtpUpdate(
    private val loginOtpRequiredLength: Int
) : Update<EnterOtpModel, EnterOtpEvent, EnterOtpEffect> {

  override fun update(
      model: EnterOtpModel,
      event: EnterOtpEvent
  ): Next<EnterOtpModel, EnterOtpEffect> {
    return when (event) {
      is UserLoaded -> next(model.userLoaded(event.user))
      is EnterOtpSubmitted -> otpSubmitted(event, model)
      is LoginUserCompleted -> loginCompleted(model, event)
      UserVerifiedInBackground -> dispatch(GoBack)
      is RequestLoginOtpCompleted -> requestOtpCompleted(model, event)
      is EnterOtpResendSmsClicked -> next(model.requestLoginOtpStarted(), RequestLoginOtp as EnterOtpEffect)
      is OtpEntryProtectedStateChanged -> effectsForStateChanged(event.stateChanged)
    }
  }

  private fun effectsForStateChanged(stateChanged: ProtectedState): Next<EnterOtpModel, EnterOtpEffect> {
    return when (stateChanged) {
      is Allowed -> dispatch(generateEffectForAllowingOtpEntry(stateChanged), AllowOtpEntry)
      is Blocked -> dispatch(ShowIncorrectOtpLimitReachedError(stateChanged.attemptsMade), BlockOtpEntryUntil(stateChanged.blockedTill))
    }
  }

  private fun generateEffectForAllowingOtpEntry(protectedState: Allowed): EnterOtpEffect {
    return if (protectedState.attemptsMade == 0) {
      HideErrors
    } else {
      ShowIncorrectOtpError(attemptsMade = protectedState.attemptsMade, attemptsRemaining = protectedState.attemptsRemaining)
    }
  }

  private fun requestOtpCompleted(
      model: EnterOtpModel,
      event: RequestLoginOtpCompleted
  ): Next<EnterOtpModel, EnterOtpEffect> {
    val updatedModel = model.requestLoginOtpFinished()

    return when (val result = event.result) {
      is ActivateUser.Result.Success -> next(updatedModel, ClearPin, ShowSmsSentMessage)
      else -> next(updatedModel.requestLoginOtpFailed(AsyncOpError.from(result)), ClearPin as EnterOtpEffect)
    }
  }

  private fun loginCompleted(
      model: EnterOtpModel,
      event: LoginUserCompleted
  ): Next<EnterOtpModel, EnterOtpEffect> {
    val updatedModel = model.loginFinished()
    val loginFailedModel = model.loginFailed()
    return when (val result = event.result) {
      LoginResult.Success -> next(updatedModel, ClearLoginEntry, TriggerSync)
      is LoginResult.ServerError -> next(loginFailedModel, FailedLoginOtpAttempt(result), ClearPin)
      LoginResult.NetworkError -> next(loginFailedModel, ShowNetworkError, ClearPin)
      LoginResult.UnexpectedError -> next(loginFailedModel, ShowUnexpectedError, ClearPin)
    }
  }

  private fun otpSubmitted(
      event: EnterOtpSubmitted,
      model: EnterOtpModel
  ): Next<EnterOtpModel, EnterOtpEffect> {
    val enteredOtp = event.otp

    val updatedModel = when (enteredOtp.length) {
      loginOtpRequiredLength -> model.enteredOtpValid()
      else -> model.enteredOtpNotRequiredLength()
    }

    return if (updatedModel.isEnteredPinInvalid) {
      next(updatedModel, ClearPin as EnterOtpEffect)
    } else {
      next(updatedModel.loginStarted(), LoginUser(enteredOtp) as EnterOtpEffect)
    }
  }
}
