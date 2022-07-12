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
      is ShowOverdueAppointments -> uiActions.showOverdueAppointments(viewEffect.overdueAppointmentsOld, viewEffect.isDiabetesManagementEnabled)
      ShowNoActiveNetworkConnectionDialog -> uiActions.showNoActiveNetworkConnectionDialog()
      is OpenSelectDownloadFormatDialog -> uiActions.openSelectDownloadFormatDialog(viewEffect.selectedAppointmentIds)
      is OpenSelectShareFormatDialog -> uiActions.openSelectShareFormatDialog(viewEffect.selectedAppointmentIds)
      is OpenSharingInProgressDialog -> uiActions.openProgressForSharingDialog(viewEffect.selectedAppointmentIds)
      OpenOverdueSearch -> uiActions.openOverdueSearch()
    }.exhaustive()
  }
}
