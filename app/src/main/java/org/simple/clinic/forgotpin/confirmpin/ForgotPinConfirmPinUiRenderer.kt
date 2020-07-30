package org.simple.clinic.forgotpin.confirmpin

import org.simple.clinic.mobius.ViewRenderer

class ForgotPinConfirmPinUiRenderer(private val ui: ForgotPinConfirmPinUi) : ViewRenderer<ForgotPinConfirmPinModel> {
  override fun render(model: ForgotPinConfirmPinModel) {
    if (model.hasUser) {
      ui.showUserName(model.user!!.fullName)
    }
  }
}
