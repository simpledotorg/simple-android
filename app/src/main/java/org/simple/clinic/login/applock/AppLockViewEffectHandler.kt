package org.simple.clinic.login.applock

import org.simple.clinic.mobius.ViewEffectsHandler

class AppLockViewEffectHandler(
    private val uiActions: AppLockUiActions
) : ViewEffectsHandler<AppLockViewEffect> {

  override fun handle(viewEffect: AppLockViewEffect) {
    // no-op
  }
}
