package org.simple.clinic.newentry

import org.simple.clinic.mobius.ViewEffectsHandler

class PatientEntryViewEffectHandler(
    private val uiActions: PatientEntryUiActions
) : ViewEffectsHandler<PatientEntryViewEffect> {

  override fun handle(viewEffect: PatientEntryViewEffect) {
    // one of these days this is gonna do something
  }
}
