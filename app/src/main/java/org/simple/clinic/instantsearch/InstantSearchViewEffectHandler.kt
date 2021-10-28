package org.simple.clinic.instantsearch

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class InstantSearchViewEffectHandler(
    private val uiActions: InstantSearchUiActions
) : ViewEffectsHandler<InstantSearchViewEffect> {

  override fun handle(viewEffect: InstantSearchViewEffect) {
    when (viewEffect) {
      is ShowAllPatients -> uiActions.showAllPatients(viewEffect.patients, viewEffect.facility)
      is ShowPatientSearchResults -> uiActions.showPatientsSearchResults(viewEffect.patients, viewEffect.facility, viewEffect.searchQuery)
      is OpenPatientSummary -> uiActions.openPatientSummary(viewEffect.patientId)
      is OpenLinkIdWithPatientScreen -> uiActions.openLinkIdWithPatientScreen(viewEffect.patientId, viewEffect.identifier)
    }.exhaustive()
  }
}
