package org.simple.clinic.bloodsugar.history

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class BloodSugarHistoryScreenViewEffectHandler(
    private val uiActions: BloodSugarHistoryScreenUiActions
) : ViewEffectsHandler<BloodSugarHistoryScreenViewEffect> {
  override fun handle(viewEffect: BloodSugarHistoryScreenViewEffect) {
    when (viewEffect) {
      is OpenBloodSugarEntrySheet -> uiActions.openBloodSugarEntrySheet(viewEffect.patientUuid)
      is OpenBloodSugarUpdateSheet -> uiActions.openBloodSugarUpdateSheet(viewEffect.bloodSugarMeasurement)
      is ShowBloodSugars -> uiActions.showBloodSugars(viewEffect.bloodSugarHistoryListItemDataSourceFactory)
    }.exhaustive()
  }
}
