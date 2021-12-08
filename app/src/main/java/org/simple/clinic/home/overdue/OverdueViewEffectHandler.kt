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
      is ShowOverdueAppointments -> uiActions.showOverdueAppointments(viewEffect.overdueAppointments, viewEffect.isDiabetesManagementEnabled)
      ShowNoActiveNetworkConnectionDialog -> uiActions.showNoActiveNetworkConnectionDialog()
      OpenSelectDownloadFormatDialog -> uiActions.openSelectDownloadFormatDialog()
      OpenSelectShareFormatDialog -> uiActions.openSelectShareFormatDialog()
      OpenSharingInProgressDialog -> uiActions.openProgressForSharingDialog()
    }.exhaustive()
  }
}
