package org.simple.clinic.registration.facility

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class RegistrationFacilitySelectionViewEffectHandler(
    private val uiActions: RegistrationFacilitySelectionUiActions
) : ViewEffectsHandler<RegistrationFacilitySelectionViewEffect>{
  override fun handle(viewEffect: RegistrationFacilitySelectionViewEffect) {
    when (viewEffect) {
      is OpenConfirmFacilitySheet -> uiActions.showConfirmFacilitySheet(viewEffect.facility.uuid, viewEffect.facility.name)
      is MoveToIntroVideoScreen -> uiActions.openIntroVideoScreen(viewEffect.registrationEntry)
    }.exhaustive()
  }
}
