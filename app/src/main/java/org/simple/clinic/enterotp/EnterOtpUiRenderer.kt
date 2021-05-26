package org.simple.clinic.enterotp

import org.simple.clinic.enterotp.ValidationResult.IsNotRequiredLength
import org.simple.clinic.enterotp.ValidationResult.NotValidated
import org.simple.clinic.enterotp.ValidationResult.Valid
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class EnterOtpUiRenderer(
    private val ui: EnterOtpUi
) : ViewRenderer<EnterOtpModel> {

  private val phoneNumberChangedCallback = ValueChangedCallback<String>()

  private val loginErrorChangedCallback = ValueChangedCallback<AsyncOpError?>()

  private val isAsyncOperationOngoingChangedCallback = ValueChangedCallback<Boolean>()

  override fun render(model: EnterOtpModel) {
    if (model.hasLoadedUser) {
      phoneNumberChangedCallback.pass(model.user!!.phoneNumber, ui::showUserPhoneNumber)
    }

    when (model.otpValidationResult) {
      NotValidated -> { /* Nothing to do here */
      }
      IsNotRequiredLength -> ui.showIncorrectOtpError()
      Valid -> { /* Nothing to do here */
      }
    }

    loginErrorChangedCallback.pass(model.asyncOpError) { loginError ->
      when (loginError) {
        NetworkError -> ui.showNetworkError()
        is ServerError -> ui.showServerError(loginError.errorMessage)
        OtherError -> ui.showUnexpectedError()
        null -> ui.hideError()
      }
    }

    isAsyncOperationOngoingChangedCallback.pass(model.isAsyncOperationOngoing) { isAsyncOperationOngoing ->
      if (isAsyncOperationOngoing)
        ui.showProgress()
      else
        ui.hideProgress()
    }
  }
}
