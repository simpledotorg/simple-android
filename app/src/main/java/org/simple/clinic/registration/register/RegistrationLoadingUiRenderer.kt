package org.simple.clinic.registration.register

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.registration.register.RegisterUserResult.NetworkError
import org.simple.clinic.registration.register.RegisterUserResult.OtherError
import org.simple.clinic.registration.register.RegisterUserResult.Success

class RegistrationLoadingUiRenderer(
    private val ui: RegistrationLoadingUi
) : ViewRenderer<RegistrationLoadingModel> {

  override fun render(model: RegistrationLoadingModel) {
    if (model.hasUserRegistrationCompleted) {
      renderRegistrationResult(model)
    }
  }

  private fun renderRegistrationResult(model: RegistrationLoadingModel) {
    when (model.registerUserResult!!) {
      Success -> {
        /* Nothing to do here */
      }
      NetworkError -> ui.showNetworkError()
      OtherError -> ui.showUnexpectedError()
    }
  }
}
