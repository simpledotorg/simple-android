package org.simple.clinic.enterotp

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.login.LoginResult
import org.simple.clinic.mobius.next

class EnterOtpUpdate(
    private val loginOtpRequiredLength: Int
) : Update<EnterOtpModel, EnterOtpEvent, EnterOtpEffect> {

  override fun update(model: EnterOtpModel, event: EnterOtpEvent): Next<EnterOtpModel, EnterOtpEffect> {
    return when (event) {
      is UserLoaded -> next(model.userLoaded(event.user))
      is EnterOtpSubmitted -> {
        val enteredOtp = event.otp

        val updatedModel = when (enteredOtp.length) {
          loginOtpRequiredLength -> model.enteredOtpValid()
          else -> model.enteredOtpNotRequiredLength()
        }

        if (updatedModel.isEnteredPinInvalid) {
          next(updatedModel, ClearPin as EnterOtpEffect)
        } else {
          next(updatedModel.loginStarted(), LoginUser(enteredOtp) as EnterOtpEffect)
        }
      }
      is LoginUserCompleted -> {
        val updatedModel = model.loginFinished()
        when (val result = event.result) {
          LoginResult.Success -> next(updatedModel, ClearLoginEntry, TriggerSync, GoBack)
          else -> next(model.loginFailed(LoginError.from(result)), ClearPin as EnterOtpEffect)
        }
      }
    }
  }
}
