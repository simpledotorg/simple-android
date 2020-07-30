package org.simple.clinic.forgotpin.confirmpin

import org.simple.clinic.facility.Facility
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.user.User
import org.simple.clinic.util.ValueChangedCallback

class ForgotPinConfirmPinUiRenderer(private val ui: ForgotPinConfirmPinUi) : ViewRenderer<ForgotPinConfirmPinModel> {

  private val userChangedCallback = ValueChangedCallback<User>()
  private val facilityChangedCallback = ValueChangedCallback<Facility>()

  override fun render(model: ForgotPinConfirmPinModel) {
    if (model.hasUser) {
      userChangedCallback.pass(model.user!!) {
        ui.showUserName(it.fullName)
      }
    }

    if (model.hasFacility) {
      facilityChangedCallback.pass(model.facility!!) {
        ui.showFacility(it.name)
      }
    }
  }
}
