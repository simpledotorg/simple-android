package org.simple.clinic.enterotp

import com.spotify.mobius.Next
import com.spotify.mobius.Update
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
    return when (val result = event.result) {
      LoginResult.Success -> next(updatedModel, ClearLoginEntry, TriggerSync)
      else -> next(updatedModel.loginFailed(AsyncOpError.from(result)), ClearPin as EnterOtpEffect)
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
