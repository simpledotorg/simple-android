package org.simple.clinic.reassignPatient

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class ReassignPatientViewEffectHandler(
    private val uiActions: ReassignPatientUiActions
) : ViewEffectsHandler<ReassignPatientViewEffect> {

  override fun handle(viewEffect: ReassignPatientViewEffect) {
    when (viewEffect) {
      is CloseSheet -> uiActions.closeSheet()
      OpenSelectFacilitySheet -> uiActions.openSelectFacilitySheet()
    }.exhaustive()
  }
}
