package org.simple.clinic.login.applock

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class AppLockViewEffectHandler(
    private val uiActions: AppLockUiActions
) : ViewEffectsHandler<AppLockViewEffect> {

  override fun handle(viewEffect: AppLockViewEffect) {
    when (viewEffect) {
      ExitApp -> uiActions.exitApp()
      ShowConfirmResetPinDialog -> uiActions.showConfirmResetPinDialog()
      RestorePreviousScreen -> uiActions.restorePreviousScreen()
    }.exhaustive()
  }
}
