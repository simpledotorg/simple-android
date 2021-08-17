package org.simple.clinic.registration.phone

import org.simple.clinic.mobius.ViewEffectsHandler

class RegistrationPhoneViewEffectHandler(
    private val uiActions: RegistrationPhoneUiActions
) : ViewEffectsHandler<RegistrationPhoneViewEffect> {

  override fun handle(viewEffect: RegistrationPhoneViewEffect) {
    when (viewEffect) {
      is PrefillFields -> uiActions.preFillUserDetails(viewEffect.entry)
      is ShowAccessDeniedScreen -> uiActions.showAccessDeniedScreen(viewEffect.number)
      ProceedToLogin -> uiActions.openLoginPinEntryScreen()
      ShowUserLoggedOutAlert -> uiActions.showLoggedOutOfDeviceDialog()
      is ContinueRegistration -> uiActions.openRegistrationNameEntryScreen(viewEffect.entry)
    }
  }
}
