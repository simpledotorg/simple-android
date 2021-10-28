package org.simple.clinic.summary

import org.simple.clinic.mobius.ViewEffectsHandler

class PatientSummaryViewEffectHandler(
    private val uiActions: PatientSummaryUiActions
) : ViewEffectsHandler<PatientSummaryViewEffect> {

  override fun handle(viewEffect: PatientSummaryViewEffect) {
    // nothing to do here yet
  }
}
