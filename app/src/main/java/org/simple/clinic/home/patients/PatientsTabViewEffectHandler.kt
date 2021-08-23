package org.simple.clinic.home.patients

import org.simple.clinic.mobius.ViewEffectsHandler

class PatientsTabViewEffectHandler(
    private val uiActions: PatientsTabUiActions
) : ViewEffectsHandler<PatientsTabViewEffect> {

  override fun handle(viewEffect: PatientsTabViewEffect) {
    // No-op
  }
}
