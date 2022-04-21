package org.simple.clinic.forgotpin.confirmpin

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class ForgotPinConfirmPinViewEffectHandler(
    private val uiActions: ForgotPinConfirmPinUiActions
) : ViewEffectsHandler<ForgotPinConfirmPinViewEffect> {

  override fun handle(viewEffect: ForgotPinConfirmPinViewEffect) {
    when (viewEffect) {
      HideError -> uiActions.hideError()
      ShowMismatchedError -> uiActions.showPinMismatchedError()
      ShowProgress -> uiActions.showProgress()
      ShowNetworkError -> uiActions.showNetworkError()
      ShowUnexpectedError -> uiActions.showUnexpectedError()
      GoToHomeScreen -> uiActions.goToHomeScreen()
    }.exhaustive()
  }
}
