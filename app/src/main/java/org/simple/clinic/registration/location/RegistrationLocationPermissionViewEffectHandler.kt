package org.simple.clinic.registration.location

import org.simple.clinic.mobius.ViewEffectsHandler

class RegistrationLocationPermissionViewEffectHandler(
    private val uiActions: RegistrationLocationPermissionUiActions
) : ViewEffectsHandler<RegistrationLocationPermissionViewEffect> {
  override fun handle(viewEffect: RegistrationLocationPermissionViewEffect) {
    when (viewEffect) {
      is OpenFacilitySelectionScreen -> uiActions.openFacilitySelectionScreen(viewEffect.entry)
    }
  }
}
