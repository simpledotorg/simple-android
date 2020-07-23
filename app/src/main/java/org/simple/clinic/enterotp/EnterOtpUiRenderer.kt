package org.simple.clinic.enterotp

import org.simple.clinic.mobius.ViewRenderer

class EnterOtpUiRenderer(
    private val ui: EnterOtpUi
) : ViewRenderer<EnterOtpModel> {

  override fun render(model: EnterOtpModel) {
    if (model.hasLoadedUser) {
      ui.showUserPhoneNumber(model.user!!.phoneNumber)
    }
  }
}
