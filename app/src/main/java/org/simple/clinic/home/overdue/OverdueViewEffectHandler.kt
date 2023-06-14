package org.simple.clinic.home.overdue

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class OverdueViewEffectHandler(
    private val uiActions: OverdueUiActions
) : ViewEffectsHandler<OverdueViewEffect> {

  override fun handle(viewEffect: OverdueViewEffect) {
    when (viewEffect) {
      is OpenContactPatientScreen -> uiActions.openPhoneMaskBottomSheet(viewEffect.patientUuid)
      is OpenPatientSummary -> uiActions.openPatientSummary(viewEffect.patientUuid)
      ShowNoActiveNetworkConnectionDialog -> uiActions.showNoActiveNetworkConnectionDialog()
      is OpenSelectDownloadFormatDialog -> uiActions.openSelectDownloadFormatDialog()
      is OpenSelectShareFormatDialog -> uiActions.openSelectShareFormatDialog()
      is OpenSharingInProgressDialog -> uiActions.openProgressForSharingDialog()
      OpenOverdueSearch -> uiActions.openOverdueSearch()
    }.exhaustive()
  }
}
