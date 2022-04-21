package org.simple.clinic.forgotpin.createnewpin

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class ForgotPinCreateNewViewEffectHandler(
    private val uiActions: UiActions
) : ViewEffectsHandler<ForgotPinCreateNewViewEffect> {

  override fun handle(viewEffect: ForgotPinCreateNewViewEffect) {
    when (viewEffect) {
      ShowInvalidPinError -> uiActions.showInvalidPinError()
      is ShowConfirmPinScreen -> uiActions.showConfirmPinScreen(viewEffect.pin)
    }.exhaustive()
  }
}
