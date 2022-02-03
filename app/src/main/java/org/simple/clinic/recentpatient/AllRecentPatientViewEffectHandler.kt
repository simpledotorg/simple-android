package org.simple.clinic.recentpatient

import org.simple.clinic.mobius.ViewEffectsHandler

class AllRecentPatientViewEffectHandler(
    private val uiActions: AllRecentPatientsUiActions
) : ViewEffectsHandler<AllRecentPatientsViewEffect> {

  override fun handle(viewEffect: AllRecentPatientsViewEffect) {
    // Empty...for now
  }
}
