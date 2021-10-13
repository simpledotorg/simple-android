package org.simple.clinic.enterotp

import org.simple.clinic.mobius.ViewEffectsHandler

class EnterOtpViewEffectHandler(
    private val uiActions: EnterOtpUiActions
) : ViewEffectsHandler<EnterOtpViewEffect> {
  override fun handle(viewEffect: EnterOtpViewEffect) {
    when (viewEffect) {
      ClearPin -> uiActions::clearPin
    }
  }
}
