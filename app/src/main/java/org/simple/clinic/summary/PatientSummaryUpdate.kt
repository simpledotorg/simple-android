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
import java.util.UUID

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
      is PatientSummaryBloodPressureSaved -> bloodPressureSaved(model.openIntention, model.patientUuid)
      is FetchedHasShownMissingReminder -> fetchedHasShownMissingReminder(event.hasShownReminder, model.patientUuid)
      else -> noChange()
    }
  }

  private fun fetchedHasShownMissingReminder(
      hasShownReminder: Boolean,
      patientUuid: UUID
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    return if (!hasShownReminder) {
      dispatch(MarkReminderAsShown(patientUuid), ShowAddPhonePopup(patientUuid))
    } else {
      noChange()
    }
  }

  private fun bloodPressureSaved(
      openIntention: OpenIntention,
      patientUuid: UUID
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    return when (openIntention) {
      ViewNewPatient -> noChange()
      else -> dispatch(FetchHasShownMissingPhoneReminder(patientUuid))
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
