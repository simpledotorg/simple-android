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
      is PatientSummaryBackClicked -> dispatch(LoadDataForBackClick(model.patientUuid, event.screenCreatedTimestamp))
      is PatientSummaryDoneClicked -> dispatch(LoadDataForDoneClick(model.patientUuid))
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility))
      PatientSummaryEditClicked -> dispatch(HandleEditClick(model.patientSummaryProfile!!))
      is PatientSummaryLinkIdCancelled -> dispatch(HandleLinkIdCancelled)
      is ScheduledAppointment -> dispatch(TriggerSync(event.sheetOpenedFrom))
      is CompletedCheckForInvalidPhone -> next(model.completedCheckForInvalidPhone())
      is PatientSummaryBloodPressureSaved -> bloodPressureSaved(model.openIntention, model.patientUuid)
      is FetchedHasShownMissingReminder_Old -> fetchedHasShownMissingReminder(event.hasShownReminder, model.patientUuid)
      is LinkIdWithPatientSheetShown -> next(model.shownLinkIdWithPatientView())
      is PatientSummaryLinkIdCompleted -> dispatch(HideLinkIdWithPatientView)
      is ReportedViewedPatientToAnalytics -> next(model.reportedViewedPatientToAnalytics())
      is DataForBackClickLoaded -> dataForHandlingBackLoaded(
          patientUuid = model.patientUuid,
          hasPatientDataChanged = event.hasPatientDataChangedSinceScreenCreated,
          countOfRecordedMeasurements = event.countOfRecordedMeasurements,
          openIntention = model.openIntention,
          diagnosisRecorded = event.diagnosisRecorded,
          isDiabetesManagementEnabled = model.isDiabetesManagementEnabled
      )
      is DataForDoneClickLoaded -> dataForHandlingDoneClickLoaded(
          patientUuid = model.patientUuid,
          countOfRecordedMeasurements = event.countOfRecordedMeasurements,
          diagnosisRecorded = event.diagnosisRecorded,
          isDiabetesManagementEnabled = model.isDiabetesManagementEnabled
      )
      is SyncTriggered -> scheduleAppointmentSheetClosed(model, event.sheetOpenedFrom)
      else -> noChange()
    }
  }

  private fun dataForHandlingDoneClickLoaded(
      patientUuid: UUID,
      countOfRecordedMeasurements: Int,
      diagnosisRecorded: Boolean,
      isDiabetesManagementEnabled: Boolean
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val hasAtLeastOneMeasurementRecorded = countOfRecordedMeasurements > 0
    val shouldShowDiagnosisError = hasAtLeastOneMeasurementRecorded && diagnosisRecorded.not() && isDiabetesManagementEnabled

    val effect = when {
      shouldShowDiagnosisError -> ShowDiagnosisError
      hasAtLeastOneMeasurementRecorded -> ShowScheduleAppointmentSheet(patientUuid, DONE_CLICK)
      else -> GoToHomeScreen
    }

    return dispatch(effect)
  }

  private fun dataForHandlingBackLoaded(
      patientUuid: UUID,
      hasPatientDataChanged: Boolean,
      countOfRecordedMeasurements: Int,
      openIntention: OpenIntention,
      diagnosisRecorded: Boolean,
      isDiabetesManagementEnabled: Boolean
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val shouldShowScheduleAppointmentSheet = if (countOfRecordedMeasurements == 0) false else hasPatientDataChanged
    val shouldShowDiagnosisError = shouldShowScheduleAppointmentSheet && diagnosisRecorded.not() && isDiabetesManagementEnabled
    val shouldGoToPreviousScreen = openIntention is ViewExistingPatient
    val shouldGoToHomeScreen = openIntention is LinkIdWithPatient || openIntention is ViewNewPatient

    val effect = when {
      shouldShowDiagnosisError -> ShowDiagnosisError
      shouldShowScheduleAppointmentSheet -> ShowScheduleAppointmentSheet(patientUuid, BACK_CLICK)
      shouldGoToPreviousScreen -> GoBackToPreviousScreen
      shouldGoToHomeScreen -> GoToHomeScreen
      else -> throw IllegalStateException("This should not happen!")
    }

    return dispatch(effect)
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
      else -> dispatch(FetchHasShownMissingPhoneReminder_Old(patientUuid))
    }
  }

  private fun scheduleAppointmentSheetClosed(
      model: PatientSummaryModel,
      appointmentScheduledFrom: AppointmentSheetOpenedFrom
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val effect = when (appointmentScheduledFrom) {
      BACK_CLICK -> when (model.openIntention) {
        ViewExistingPatient -> GoBackToPreviousScreen
        ViewNewPatient, is LinkIdWithPatient -> GoToHomeScreen
      }
      DONE_CLICK -> GoToHomeScreen
    }

    return dispatch(effect)
  }
}
