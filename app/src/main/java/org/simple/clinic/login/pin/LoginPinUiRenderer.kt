package org.simple.clinic.login.pin

import org.simple.clinic.mobius.ViewRenderer

class LoginPinUiRenderer(private val ui: LoginPinScreenUi) : ViewRenderer<LoginPinModel> {

  override fun render(model: LoginPinModel) {
    if (model.hasOngoingLoginEntry) {
      ui.showPhoneNumber(model.ongoingLoginEntry!!.phoneNumber!!)
    }
  }
}
