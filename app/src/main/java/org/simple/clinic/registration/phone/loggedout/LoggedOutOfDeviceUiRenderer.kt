package org.simple.clinic.registration.phone.loggedout

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.user.UserSession.LogoutResult.Success

class LoggedOutOfDeviceUiRenderer(private val ui: LoggedOutOfDeviceDialogUi) : ViewRenderer<LoggedOutOfDeviceModel> {
  override fun render(model: LoggedOutOfDeviceModel) {
    if (model.hasLogoutResult.not()) {
      ui.disableOkayButton()
    } else {
      renderOkayButton(model)
    }
  }

  private fun renderOkayButton(model: LoggedOutOfDeviceModel) {
    when (model.logoutResult!!) {
      is Success -> ui.enableOkayButton()
    }
  }
}
