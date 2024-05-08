package org.simple.clinic.summary

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.reassignpatient.ReassignPatientSheetOpenedFrom
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.BACK_CLICK
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.DONE_CLICK
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.NEXT_APPOINTMENT_ACTION_CLICK
import org.simple.clinic.summary.ClickAction.BACK
import org.simple.clinic.summary.ClickAction.DONE
import org.simple.clinic.summary.OpenIntention.LinkIdWithPatient
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import org.simple.clinic.summary.OpenIntention.ViewExistingPatientWithTeleconsultLog
import org.simple.clinic.summary.OpenIntention.ViewNewPatient
import java.util.UUID

class PatientSummaryUpdate(
    private val isPatientReassignmentFeatureEnabled: Boolean
) : Update<PatientSummaryModel, PatientSummaryEvent, PatientSummaryEffect> {

  override fun update(
      model: PatientSummaryModel,
      event: PatientSummaryEvent
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    return when (event) {
      is PatientSummaryProfileLoaded -> patientSummaryProfileLoaded(model, event)
      is PatientSummaryBackClicked -> backClicked(model, event)
      is PatientSummaryDoneClicked -> doneClicked(model, event)
      is CurrentUserAndFacilityLoaded -> currentUserAndFacilityLoaded(model, event)
      PatientSummaryEditClicked -> dispatch(HandleEditClick(model.patientSummaryProfile!!, model.currentFacility!!))
      is ScheduledAppointment -> dispatch(TriggerSync(event.sheetOpenedFrom))
      is CompletedCheckForInvalidPhone -> completedCheckForInvalidPhone(model, event)
      is PatientSummaryBloodPressureSaved -> bloodPressureSaved(model.openIntention, model.patientSummaryProfile!!)
      is FetchedHasShownMissingPhoneReminder -> fetchedHasShownMissingReminder(event.hasShownReminder, model.patientUuid)
      is DataForBackClickLoaded -> dataForHandlingBackLoaded(
          model = model,
          hasPatientMeasurementDataChangedSinceScreenCreated = event.hasPatientMeasurementDataChangedSinceScreenCreated,
          hasAppointmentChangedSinceScreenCreated = event.hasAppointmentChangeSinceScreenCreated,
          countOfRecordedBloodPressures = event.countOfRecordedBloodPressures,
          countOfRecordedBloodSugars = event.countOfRecordedBloodSugars,
          medicalHistory = event.medicalHistory,
          isPatientEligibleForReassignment = event.isPatientEligibleForReassignment
      )

      is DataForDoneClickLoaded -> dataForHandlingDoneClickLoaded(
          model = model,
          countOfRecordedBloodPressures = event.countOfRecordedBloodPressures,
          countOfRecordedBloodSugars = event.countOfRecordedBloodSugars,
          medicalHistory = event.medicalHistory,
          hasPatientMeasurementDataChangedSinceScreenCreated = event.hasPatientMeasurementDataChangedSinceScreenCreated,
          hasAppointmentChangedSinceScreenCreated = event.hasAppointmentChangeSinceScreenCreated,
          isPatientEligibleForReassignment = event.isPatientEligibleForReassignment
      )

      is SyncTriggered -> scheduleAppointmentSheetClosed(model, event.sheetOpenedFrom)
      is ContactPatientClicked -> dispatch(OpenContactPatientScreen(model.patientUuid))
      ContactDoctorClicked -> dispatch(OpenContactDoctorSheet(model.patientUuid))
      LogTeleconsultClicked -> logTeleconsultClicked(model)
      is MedicalOfficersLoaded -> next(model.medicalOfficersLoaded(event.medicalOfficers))
      ChangeAssignedFacilityClicked -> dispatch(OpenSelectFacilitySheet)
      is NewAssignedFacilitySelected -> dispatch(DispatchNewAssignedFacility(event.facility))
      is PatientRegistrationDataLoaded -> patientRegistrationDataLoaded(model, event)
      NextAppointmentActionClicked -> dispatch(ShowScheduleAppointmentSheet(
          model.patientUuid,
          NEXT_APPOINTMENT_ACTION_CLICK,
          model.currentFacility!!
      ))

      AssignedFacilityChanged -> dispatch(RefreshNextAppointment)
      is ClinicalDecisionSupportInfoLoaded -> next(
          model.clinicalDecisionSupportInfoLoaded(event.isNewestBpEntryHigh, event.hasPrescribedDrugsChangedToday)
      )

      is CDSSPilotStatusChecked -> cdssPilotStatusChecked(event, model)
      is LatestScheduledAppointmentLoaded -> next(model.scheduledAppointmentLoaded(event.appointment))
      is MeasurementWarningNotNowClicked -> measurementWarningNotNowClicked(model, event)
      is PatientReassignmentStatusLoaded -> patientReassignmentStatusLoaded(model, event)
    }
  }

  private fun patientReassignmentStatusLoaded(
      model: PatientSummaryModel,
      event: PatientReassignmentStatusLoaded
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val effect: PatientSummaryEffect = when (event.clickAction) {
      DONE -> LoadDataForDoneClick(
          patientUuid = model.patientUuid,
          screenCreatedTimestamp = event.screenCreatedTimestamp,
          patientEligibleForReassignment = event.isPatientEligibleForReassignment
      )
      BACK -> LoadDataForBackClick(
          patientUuid = model.patientUuid,
          screenCreatedTimestamp = event.screenCreatedTimestamp,
          canShowPatientReassignmentWarning = event.isPatientEligibleForReassignment
      )
    }

    return dispatch(
        UpdatePatientReassignmentStatus(
            patientUuid = model.patientUuid,
            status = event.isPatientEligibleForReassignment
        ),
        effect
    )
  }

  private fun measurementWarningNotNowClicked(
      model: PatientSummaryModel,
      event: MeasurementWarningNotNowClicked
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val effect = when {
      model.hasPatientDied -> GoBackToPreviousScreen
      !isPatientReassignmentFeatureEnabled -> LoadDataForBackClick(
          patientUuid = model.patientUuid,
          screenCreatedTimestamp = event.screenCreatedTimestamp,
          canShowPatientReassignmentWarning = false
      )
      else -> CheckPatientReassignmentStatus(
          patientUuid = model.patientUuid,
          clickAction = BACK,
          screenCreatedTimestamp = event.screenCreatedTimestamp
      )
    }

    return dispatch(effect)
  }

  private fun cdssPilotStatusChecked(
      event: CDSSPilotStatusChecked,
      model: PatientSummaryModel
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    return if (event.isPilotEnabledForFacility) {
      dispatch(
          LoadClinicalDecisionSupportInfo(model.patientUuid),
          LoadLatestScheduledAppointment(model.patientUuid)
      )
    } else {
      noChange()
    }
  }

  private fun patientRegistrationDataLoaded(
      model: PatientSummaryModel,
      event: PatientRegistrationDataLoaded
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val measurementsCount = event.countOfPrescribedDrugs + event.countOfRecordedBloodPressures + event.countOfRecordedBloodSugars
    val hasRegistrationData = measurementsCount > 0

    return next(model.patientRegistrationDataLoaded(hasPatientRegistrationData = hasRegistrationData))
  }

  private fun completedCheckForInvalidPhone(
      model: PatientSummaryModel,
      event: CompletedCheckForInvalidPhone
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    return if (event.isPhoneInvalid) {
      next(model.completedCheckForInvalidPhone(), setOf(ShowUpdatePhonePopup(model.patientUuid)))
    } else {
      next(model.completedCheckForInvalidPhone())
    }
  }

  private fun doneClicked(model: PatientSummaryModel, event: PatientSummaryDoneClicked): Next<PatientSummaryModel, PatientSummaryEffect> {
    val effect = when {
      model.hasPatientDied -> GoToHomeScreen
      !isPatientReassignmentFeatureEnabled -> LoadDataForDoneClick(
          patientUuid = model.patientUuid,
          screenCreatedTimestamp = event.screenCreatedTimestamp,
          patientEligibleForReassignment = false
      )
      else -> CheckPatientReassignmentStatus(
          patientUuid = model.patientUuid,
          clickAction = DONE,
          screenCreatedTimestamp = event.screenCreatedTimestamp
      )
    }

    return dispatch(effect)
  }

  private fun backClicked(model: PatientSummaryModel, event: PatientSummaryBackClicked): Next<PatientSummaryModel, PatientSummaryEffect> {
    val effect = when {
      model.hasPatientDied -> GoBackToPreviousScreen
      !isPatientReassignmentFeatureEnabled -> LoadDataForBackClick(
          patientUuid = model.patientUuid,
          screenCreatedTimestamp = event.screenCreatedTimestamp,
          canShowPatientReassignmentWarning = false
      )
      else -> CheckPatientReassignmentStatus(
          patientUuid = model.patientUuid,
          clickAction = BACK,
          screenCreatedTimestamp = event.screenCreatedTimestamp
      )
    }

    return dispatch(effect)
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
      hasPatientMeasurementDataChangedSinceScreenCreated: Boolean,
      hasAppointmentChangedSinceScreenCreated: Boolean,
      isPatientEligibleForReassignment: Boolean,
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val canShowAppointmentSheet = hasPatientMeasurementDataChangedSinceScreenCreated && !hasAppointmentChangedSinceScreenCreated
    val hasAtLeastOneMeasurementRecorded = countOfRecordedBloodPressures + countOfRecordedBloodSugars > 0
    val shouldShowDiagnosisError = hasAtLeastOneMeasurementRecorded && medicalHistory.diagnosisRecorded.not() && model.isDiabetesManagementEnabled
    val measurementWarningEffect = validateMeasurements(
        isDiabetesManagementEnabled = model.isDiabetesManagementEnabled,
        countOfRecordedBloodSugars = countOfRecordedBloodSugars,
        countOfRecordedBloodPressures = countOfRecordedBloodPressures,
        medicalHistory = medicalHistory,
        hasShownMeasurementsWarningDialog = model.hasShownMeasurementsWarningDialog
    )

    return when {
      shouldShowDiagnosisError -> dispatch(ShowDiagnosisError)
      measurementWarningEffect != null -> next(model.shownMeasurementsWarningDialog(), setOf(measurementWarningEffect))
      isPatientEligibleForReassignment -> dispatch(ShowReassignPatientWarningSheet(model.patientUuid, model.currentFacility!!, ReassignPatientSheetOpenedFrom.DONE_CLICK))
      canShowAppointmentSheet -> dispatch(ShowScheduleAppointmentSheet(model.patientUuid, DONE_CLICK, model.currentFacility!!))
      else -> dispatch(GoToHomeScreen)
    }
  }

  private fun dataForHandlingBackLoaded(
      model: PatientSummaryModel,
      hasPatientMeasurementDataChangedSinceScreenCreated: Boolean,
      hasAppointmentChangedSinceScreenCreated: Boolean,
      countOfRecordedBloodPressures: Int,
      countOfRecordedBloodSugars: Int,
      medicalHistory: MedicalHistory,
      isPatientEligibleForReassignment: Boolean,
  ): Next<PatientSummaryModel, PatientSummaryEffect> {
    val openIntention = model.openIntention
    val canShowAppointmentSheet = hasPatientMeasurementDataChangedSinceScreenCreated && !hasAppointmentChangedSinceScreenCreated
    val hasAtLeastOneMeasurementRecorded = countOfRecordedBloodPressures + countOfRecordedBloodSugars > 0
    val shouldShowDiagnosisError = hasAtLeastOneMeasurementRecorded && medicalHistory.diagnosisRecorded.not() && model.isDiabetesManagementEnabled
    val shouldGoToPreviousScreen = openIntention is ViewExistingPatient
    val shouldGoToHomeScreen = openIntention is LinkIdWithPatient || openIntention is ViewNewPatient || openIntention is ViewExistingPatientWithTeleconsultLog
    val measurementWarningEffect = validateMeasurements(
        isDiabetesManagementEnabled = model.isDiabetesManagementEnabled,
        countOfRecordedBloodSugars = countOfRecordedBloodSugars,
        countOfRecordedBloodPressures = countOfRecordedBloodPressures,
        medicalHistory = medicalHistory,
        hasShownMeasurementsWarningDialog = model.hasShownMeasurementsWarningDialog)

    return when {
      shouldShowDiagnosisError -> dispatch(ShowDiagnosisError)
      measurementWarningEffect != null -> next(model.shownMeasurementsWarningDialog(), setOf(measurementWarningEffect))
      isPatientEligibleForReassignment -> dispatch(ShowReassignPatientWarningSheet(model.patientUuid, model.currentFacility!!, ReassignPatientSheetOpenedFrom.BACK_CLICK))
      canShowAppointmentSheet -> dispatch(ShowScheduleAppointmentSheet(model.patientUuid, BACK_CLICK, model.currentFacility!!))
      shouldGoToPreviousScreen -> dispatch(GoBackToPreviousScreen)
      shouldGoToHomeScreen -> dispatch(GoToHomeScreen)
      else -> throw IllegalStateException("This should not happen!")
    }
  }

  private fun validateMeasurements(
      isDiabetesManagementEnabled: Boolean,
      countOfRecordedBloodSugars: Int,
      countOfRecordedBloodPressures: Int,
      medicalHistory: MedicalHistory,
      hasShownMeasurementsWarningDialog: Boolean
  ): PatientSummaryEffect? {
    // If the measurements warning dialog is already shown, we don't want to show it again in the
    // same session.
    if (hasShownMeasurementsWarningDialog) return null

    val hasAtLeastOneMeasurementRecorded = countOfRecordedBloodPressures + countOfRecordedBloodSugars > 0
    val shouldShowAddMeasurementsWarning = medicalHistory.diagnosedWithHypertension == Yes && medicalHistory.diagnosedWithDiabetes == Yes && !hasAtLeastOneMeasurementRecorded
    val shouldShowAddBloodPressureWarning = medicalHistory.diagnosedWithHypertension == Yes && countOfRecordedBloodPressures == 0
    val shouldShownAddBloodSugarWarning = medicalHistory.diagnosedWithDiabetes == Yes && countOfRecordedBloodSugars == 0 && isDiabetesManagementEnabled

    return when {
      shouldShowAddMeasurementsWarning -> ShowAddMeasurementsWarningDialog
      shouldShowAddBloodPressureWarning -> ShowAddBloodPressureWarningDialog
      shouldShownAddBloodSugarWarning -> ShowAddBloodSugarWarningDialog
      else -> null
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
      NEXT_APPOINTMENT_ACTION_CLICK -> RefreshNextAppointment
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
