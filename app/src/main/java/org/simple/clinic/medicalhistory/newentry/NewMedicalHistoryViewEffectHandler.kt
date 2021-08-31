package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.mobius.ViewEffectsHandler

class NewMedicalHistoryViewEffectHandler(
    private val uiActions: NewMedicalHistoryUiActions
) : ViewEffectsHandler<NewMedicalHistoryViewEffect> {

  override fun handle(viewEffect: NewMedicalHistoryViewEffect) {
    // no-op
  }
}
