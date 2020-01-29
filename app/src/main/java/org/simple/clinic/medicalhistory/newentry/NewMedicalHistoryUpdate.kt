package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class NewMedicalHistoryUpdate : Update<NewMedicalHistoryModel, NewMedicalHistoryEvent, NewMedicalHistoryEffect> {

  override fun update(model: NewMedicalHistoryModel, event: NewMedicalHistoryEvent): Next<NewMedicalHistoryModel, NewMedicalHistoryEffect> {
    return when (event) {
      is NewMedicalHistoryAnswerToggled -> next(model.answerChanged(event.question, event.answer))
      is SaveMedicalHistoryClicked -> saveClicked(model)
      is PatientRegistered -> dispatch(OpenPatientSummaryScreen(event.patientUuid))
      is OngoingPatientEntryLoaded -> next(model.ongoingPatientEntryLoaded(event.ongoingNewPatientEntry))
      is CurrentFacilityLoaded -> currentFacilityLoaded(event, model)
    }
  }

  private fun saveClicked(model: NewMedicalHistoryModel): Next<NewMedicalHistoryModel, NewMedicalHistoryEffect> {
    return if (!model.facilityDiabetesManagementEnabled || model.hasAnsweredBothDiagnosisQuestions) {
      dispatch(RegisterPatient(model.ongoingMedicalHistoryEntry))
    } else {
      next(model.diagnosisRequired())
    }
  }

  private fun currentFacilityLoaded(
      event: CurrentFacilityLoaded,
      model: NewMedicalHistoryModel
  ): Next<NewMedicalHistoryModel, NewMedicalHistoryEffect> {
    val diabetesManagementEnabled = event.facility.config.diabetesManagementEnabled

    val updatedModel = if (diabetesManagementEnabled) {
      model.currentFacilityLoaded(event.facility)
    } else {
      model
          .currentFacilityLoaded(event.facility)
          .answerChanged(DIAGNOSED_WITH_HYPERTENSION, Yes)
    }

    return next(
        updatedModel,
        SetupUiForDiabetesManagement(diabetesManagementEnabled)
    )
  }
}
