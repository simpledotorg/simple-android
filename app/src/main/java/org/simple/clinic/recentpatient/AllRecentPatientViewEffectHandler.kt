package org.simple.clinic.recentpatient

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class AllRecentPatientViewEffectHandler(
    private val uiActions: AllRecentPatientsUiActions
) : ViewEffectsHandler<AllRecentPatientsViewEffect> {

  override fun handle(viewEffect: AllRecentPatientsViewEffect) {
    when (viewEffect) {
      is OpenPatientSummary -> uiActions.openPatientSummary(viewEffect.patientUuid)
      is ShowRecentPatients -> uiActions.showRecentPatients(viewEffect.recentPatients)
    }.exhaustive()
  }
}
