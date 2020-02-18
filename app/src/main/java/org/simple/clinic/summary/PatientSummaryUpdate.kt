package org.simple.clinic.summary

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.BACK_CLICK
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.DONE_CLICK
import org.simple.clinic.summary.OpenIntention.LinkIdWithPatient
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import org.simple.clinic.summary.OpenIntention.ViewNewPatient

class PatientSummaryUpdate : Update<PatientSummaryModel, PatientSummaryEvent, PatientSummaryEffect> {

  override fun update(model: PatientSummaryModel, event: PatientSummaryEvent): Next<PatientSummaryModel, PatientSummaryEffect> {
    return when (event) {
      is PatientSummaryProfileLoaded -> next(model.patientSummaryProfileLoaded(event.patientSummaryProfile))
      is PatientSummaryBackClicked -> dispatch(HandleBackClick(event.patientUuid, event.screenCreatedTimestamp, model.openIntention))
      is PatientSummaryDoneClicked -> dispatch(HandleDoneClick(event.patientUuid))
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility))
      PatientSummaryEditClicked -> dispatch(HandleEditClick(model.patientSummaryProfile!!))
      is PatientSummaryLinkIdCancelled -> dispatch(HandleLinkIdCancelled)
      is ScheduleAppointmentSheetClosed -> scheduleAppointmentSheetClosed(event, model)
      is CompletedCheckForInvalidPhone -> next(model.completedCheckForInvalidPhone())
      else -> noChange()
    }
  }

  private fun scheduleAppointmentSheetClosed(
      event: ScheduleAppointmentSheetClosed,
      model: PatientSummaryModel
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val effect = when (event.sheetOpenedFrom) {
      BACK_CLICK -> when (model.openIntention) {
        ViewExistingPatient -> GoBackToPreviousScreen
        ViewNewPatient, is LinkIdWithPatient -> GoToHomeScreen
      }
      DONE_CLICK -> GoToHomeScreen
    }

    return dispatch(effect)
  }
}
