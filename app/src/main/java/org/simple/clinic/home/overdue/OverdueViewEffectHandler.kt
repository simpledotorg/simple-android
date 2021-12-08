package org.simple.clinic.home.overdue

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class OverdueViewEffectHandler(
    private val uiActions: OverdueUiActions
) : ViewEffectsHandler<OverdueViewEffect> {

  override fun handle(viewEffect: OverdueViewEffect) {
    when (viewEffect) {
      is OpenContactPatientScreen -> uiActions.openPhoneMaskBottomSheet(viewEffect.patientUuid)
    }.exhaustive()
  }
}
