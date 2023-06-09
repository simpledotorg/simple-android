package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.mobius.ViewEffectsHandler

class BloodSugarEntryViewEffectHandler(
    private val uiActions: BloodSugarEntryUiActions
) : ViewEffectsHandler<BloodSugarEntryViewEffect> {

  override fun handle(viewEffect: BloodSugarEntryViewEffect) {
    // no-op
  }
}
