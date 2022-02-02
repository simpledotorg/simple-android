package org.simple.clinic.registration.facility

import org.simple.clinic.mobius.ViewEffectsHandler

class RegistrationFacilitySelectionViewEffectHandler(
    private val uiActions: RegistrationFacilitySelectionUiActions
) : ViewEffectsHandler<RegistrationFacilitySelectionViewEffect>{
  override fun handle(viewEffect: RegistrationFacilitySelectionViewEffect) {
    when(viewEffect){
      is OpenConfirmFacilitySheet -> uiActions.showConfirmFacilitySheet(viewEffect.facility.uuid, viewEffect.facility.name)
    }
  }
}
