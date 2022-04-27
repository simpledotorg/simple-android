package org.simple.clinic.bp.history

import org.simple.clinic.mobius.ViewEffectsHandler

class BloodPressureHistoryViewEffectHandler(
    private val uiActions: BloodPressureHistoryScreenUiActions
) : ViewEffectsHandler<BloodPressureHistoryViewEffect> {

  override fun handle(viewEffect: BloodPressureHistoryViewEffect) {
    // no-op
  }
}
