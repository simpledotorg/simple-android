package org.simple.clinic.home.overdue

import org.simple.clinic.home.HomeScreenViewEffect
import org.simple.clinic.mobius.ViewEffectsHandler

class OverdueViewEffectHandler(
    private val uiActions: OverdueUiActions
) : ViewEffectsHandler<OverdueViewEffect> {

  override fun handle(viewEffect: OverdueViewEffect) {
    // does nothing, yet.
  }
}
