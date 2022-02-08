package org.simple.clinic.login.pin

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class LoginPinViewEffectHandler(
    private val uiActions: UiActions
) : ViewEffectsHandler<LoginPinViewEffect> {

  override fun handle(viewEffect: LoginPinViewEffect) {
    when (viewEffect) {
      OpenHomeScreen -> uiActions.openHomeScreen()
    }.exhaustive()
  }
}
