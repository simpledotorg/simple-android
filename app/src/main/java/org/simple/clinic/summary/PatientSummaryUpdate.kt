package org.simple.clinic.summary

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.BACK_CLICK
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.DONE_CLICK
import org.simple.clinic.summary.OpenIntention.LinkIdWithPatient
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import org.simple.clinic.summary.OpenIntention.ViewExistingPatientWithTeleconsultLog
import org.simple.clinic.summary.OpenIntention.ViewNewPatient
import java.util.UUID

class PatientSummaryUpdate : Update<PatientSummaryModel, PatientSummaryEvent, PatientSummaryEffect> {

  override fun update(
      model: PatientSummaryModel,
      event: PatientSummaryEvent
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    return when (event) {
      is PatientSummaryProfileLoaded -> patientSummaryProfileLoaded(model, event)
      is PatientSummaryBackClicked -> dispatch(LoadDataForBackClick(model.patientUuid, event.screenCreatedTimestamp))
      is PatientSummaryDoneClicked -> dispatch(LoadDataForDoneClick(model.patientUuid))
      is CurrentUserAndFacilityLoaded -> currentUserAndFacilityLoaded(model, event)
      PatientSummaryEditClicked -> dispatch(HandleEditClick(model.patientSummaryProfile!!, model.currentFacility!!))
      is ScheduledAppointment -> dispatch(TriggerSync(event.sheetOpenedFrom))
      is CompletedCheckForInvalidPhone -> next(model.completedCheckForInvalidPhone())
      is PatientSummaryBloodPressureSaved -> bloodPressureSaved(model.openIntention, model.patientSummaryProfile!!)
      is FetchedHasShownMissingPhoneReminder -> fetchedHasShownMissingReminder(event.hasShownReminder, model.patientUuid)
      is DataForBackClickLoaded -> dataForHandlingBackLoaded(
          patientUuid = model.patientUuid,
          hasPatientDataChanged = event.hasPatientDataChangedSinceScreenCreated,
          countOfRecordedBloodPressures = event.countOfRecordedBloodPressures,
          countOfRecordedBloodSugars = event.countOfRecordedBloodSugars,
          openIntention = model.openIntention,
          medicalHistory = event.medicalHistory,
          isDiabetesManagementEnabled = model.isDiabetesManagementEnabled,
          currentFacility = model.currentFacility!!
      )
      is DataForDoneClickLoaded -> dataForHandlingDoneClickLoaded(
          model = model,
          countOfRecordedBloodPressures = event.countOfRecordedBloodPressures,
          countOfRecordedBloodSugars = event.countOfRecordedBloodSugars,
          medicalHistory = event.medicalHistory
      )
      is SyncTriggered -> scheduleAppointmentSheetClosed(model, event.sheetOpenedFrom)
      is ContactPatientClicked -> dispatch(OpenContactPatientScreen(model.patientUuid))
      ContactDoctorClicked -> dispatch(OpenContactDoctorSheet(model.patientUuid))
      LogTeleconsultClicked -> logTeleconsultClicked(model)
      is MedicalOfficersLoaded -> next(model.medicalOfficersLoaded(event.medicalOfficers))
    }
  }

  private fun patientSummaryProfileLoaded(
      model: PatientSummaryModel,
      event: PatientSummaryProfileLoaded
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val effects = mutableSetOf<PatientSummaryEffect>()
    if (model.openIntention is LinkIdWithPatient &&
        !event.patientSummaryProfile.hasIdentifier(model.openIntention.identifier)) {
      effects.add(ShowLinkIdWithPatientView(model.patientUuid, model.openIntention.identifier))
    }

    return next(model.patientSummaryProfileLoaded(event.patientSummaryProfile), effects)
  }

  private fun logTeleconsultClicked(model: PatientSummaryModel): Next<PatientSummaryModel, PatientSummaryEffect> {
    val openIntention = model.openIntention as ViewExistingPatientWithTeleconsultLog
    return dispatch(NavigateToTeleconsultRecordScreen(
        patientUuid = model.patientUuid,
        teleconsultRecordId = openIntention.teleconsultRecordId
    ))
  }

  private fun currentUserAndFacilityLoaded(
      model: PatientSummaryModel,
      event: CurrentUserAndFacilityLoaded
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val updatedModel = model
        .userLoggedInStatusLoaded(event.user.loggedInStatus)
        .currentFacilityLoaded(event.facility)

    return next(updatedModel)
  }

  private fun dataForHandlingDoneClickLoaded(
      model: PatientSummaryModel,
      countOfRecordedBloodPressures: Int,
      countOfRecordedBloodSugars: Int,
      medicalHistory: MedicalHistory,
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val hasAtLeastOneMeasurementRecorded = countOfRecordedBloodPressures + countOfRecordedBloodSugars > 0
    val shouldShowDiagnosisError = hasAtLeastOneMeasurementRecorded && medicalHistory.diagnosisRecorded.not() && model.isDiabetesManagementEnabled
    val shouldShowAddMeasurementsWarning = medicalHistory.diagnosedWithHypertension == Yes && medicalHistory.diagnosedWithDiabetes == Yes && !hasAtLeastOneMeasurementRecorded
    val shouldShowAddBloodPressureWarning = medicalHistory.diagnosedWithHypertension == Yes && countOfRecordedBloodPressures == 0

    return when {
      shouldShowDiagnosisError -> dispatch(ShowDiagnosisError)
      shouldShowAddMeasurementsWarning && !model.hasShownMeasurementsWarningDialog -> next(model.shownMeasurementsWarningDialog(), setOf(ShowAddMeasurementsWarningDialog))
      shouldShowAddBloodPressureWarning && !model.hasShownMeasurementsWarningDialog -> next(model.shownMeasurementsWarningDialog(), setOf(ShowAddBloodPressureWarningDialog))
      hasAtLeastOneMeasurementRecorded -> dispatch(ShowScheduleAppointmentSheet(model.patientUuid, DONE_CLICK, model.currentFacility!!))
      else -> dispatch(GoToHomeScreen)
    }
  }

  private fun dataForHandlingBackLoaded(
      patientUuid: UUID,
      hasPatientDataChanged: Boolean,
      countOfRecordedBloodPressures: Int,
      countOfRecordedBloodSugars: Int,
      openIntention: OpenIntention,
      medicalHistory: MedicalHistory,
      isDiabetesManagementEnabled: Boolean,
      currentFacility: Facility
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val shouldShowScheduleAppointmentSheet = if (countOfRecordedBloodPressures + countOfRecordedBloodSugars == 0) false else hasPatientDataChanged
    val shouldShowDiagnosisError = shouldShowScheduleAppointmentSheet && medicalHistory.diagnosisRecorded.not() && isDiabetesManagementEnabled
    val shouldGoToPreviousScreen = openIntention is ViewExistingPatient
    val shouldGoToHomeScreen = openIntention is LinkIdWithPatient || openIntention is ViewNewPatient || openIntention is ViewExistingPatientWithTeleconsultLog

    val effect = when {
      shouldShowDiagnosisError -> ShowDiagnosisError
      shouldShowScheduleAppointmentSheet -> ShowScheduleAppointmentSheet(patientUuid, BACK_CLICK, currentFacility)
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
      patientSummaryProfile: PatientSummaryProfile
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    return when (openIntention) {
      ViewNewPatient -> noChange()
      else -> if (!patientSummaryProfile.hasPhoneNumber) {
        dispatch(FetchHasShownMissingPhoneReminder(patientSummaryProfile.patient.uuid) as PatientSummaryEffect)
      } else {
        noChange()
      }
    }
  }

  private fun scheduleAppointmentSheetClosed(
      model: PatientSummaryModel,
      appointmentScheduledFrom: AppointmentSheetOpenedFrom
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val effect = when (appointmentScheduledFrom) {
      BACK_CLICK -> handleBackClick(model)
      DONE_CLICK -> GoToHomeScreen
    }

    return dispatch(effect)
  }

  private fun handleBackClick(model: PatientSummaryModel): PatientSummaryEffect {
    return when (model.openIntention) {
      ViewExistingPatient -> GoBackToPreviousScreen
      ViewNewPatient, is LinkIdWithPatient -> GoToHomeScreen
      is ViewExistingPatientWithTeleconsultLog -> GoToHomeScreen
    }
  }
}
