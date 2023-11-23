package org.simple.clinic.facility.alertchange

import org.simple.clinic.facility.alertchange.AlertFacilityChangeViewEffect.CloseSheetWithContinuation
import org.simple.clinic.mobius.ViewEffectsHandler

class AlertFacilityChangeViewEffectHandler(
    private val uiAction: UiActions
) : ViewEffectsHandler<AlertFacilityChangeViewEffect> {

  override fun handle(viewEffect: AlertFacilityChangeViewEffect) {
    when (viewEffect) {
      CloseSheetWithContinuation -> uiAction.closeSheetWithContinuation()
    }
  }
}
