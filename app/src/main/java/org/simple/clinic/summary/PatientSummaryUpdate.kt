package org.simple.clinic.summary

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.BACK_CLICK
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient

class PatientSummaryUpdate : Update<PatientSummaryModel, PatientSummaryEvent, PatientSummaryEffect> {

  override fun update(model: PatientSummaryModel, event: PatientSummaryEvent): Next<PatientSummaryModel, PatientSummaryEffect> {
    return when (event) {
      is PatientSummaryProfileLoaded -> next(model.patientSummaryProfileLoaded(event.patientSummaryProfile))
      is PatientSummaryBackClicked -> dispatch(HandleBackClick(event.patientUuid, event.screenCreatedTimestamp))
      is PatientSummaryDoneClicked -> dispatch(HandleDoneClick(event.patientUuid))
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility))
      PatientSummaryEditClicked -> dispatch(HandleEditClick(model.patientSummaryProfile!!))
      is PatientSummaryLinkIdCancelled -> dispatch(HandleLinkIdCancelled)
      is ScheduleAppointmentSheetClosed -> {
        val sheetOpenedFrom = event.sheetOpenedFrom
        if (sheetOpenedFrom == BACK_CLICK && model.openIntention == ViewExistingPatient) {
          dispatch(GoBackToPreviousScreen as PatientSummaryEffect)
        } else {
          noChange()
        }
      }
      else -> noChange()
    }
  }
}
