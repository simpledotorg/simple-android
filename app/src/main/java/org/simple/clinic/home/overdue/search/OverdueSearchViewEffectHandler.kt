package org.simple.clinic.home.overdue.search

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class OverdueSearchViewEffectHandler(
    private val uiActions: OverdueSearchUiActions
) : ViewEffectsHandler<OverdueSearchViewEffect> {

  override fun handle(viewEffect: OverdueSearchViewEffect) {
    when (viewEffect) {
      is OpenPatientSummary -> uiActions.openPatientSummaryScreen(viewEffect.patientUuid)
      is OpenContactPatientSheet -> uiActions.openContactPatientSheet(viewEffect.patientUuid)
      is SetOverdueSearchQuery -> uiActions.setOverdueSearchQuery(viewEffect.searchQuery)
    }.exhaustive()
  }
}
