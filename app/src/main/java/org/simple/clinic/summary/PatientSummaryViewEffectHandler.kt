package org.simple.clinic.summary

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class PatientSummaryViewEffectHandler(
    private val uiActions: PatientSummaryUiActions
) : ViewEffectsHandler<PatientSummaryViewEffect> {

  override fun handle(viewEffect: PatientSummaryViewEffect) {
    when (viewEffect) {
      is HandleEditClick -> uiActions.showEditPatientScreen(viewEffect.patientSummaryProfile, viewEffect.currentFacility)
      GoBackToPreviousScreen -> uiActions.goToPreviousScreen()
    }.exhaustive()
  }
}
