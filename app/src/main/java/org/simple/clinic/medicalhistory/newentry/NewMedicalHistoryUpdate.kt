package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class NewMedicalHistoryUpdate : Update<NewMedicalHistoryModel, NewMedicalHistoryEvent, NewMedicalHistoryEffect> {

  override fun update(
      model: NewMedicalHistoryModel,
      event: NewMedicalHistoryEvent
  ): Next<NewMedicalHistoryModel, NewMedicalHistoryEffect> {
    return when (event) {
      is NewMedicalHistoryAnswerToggled -> answerToggled(model, event.question, event.answer)
      is SaveMedicalHistoryClicked -> saveClicked(model)
      is PatientRegistered -> next(model.patientRegistered(), TriggerSync(event.patientUuid))
      is OngoingPatientEntryLoaded -> next(model.ongoingPatientEntryLoaded(event.ongoingNewPatientEntry))
      is CurrentFacilityLoaded -> currentFacilityLoaded(event, model)
      is SyncTriggered -> dispatch(OpenPatientSummaryScreen(event.registeredPatientUuid))
      else -> noChange()
    }
  }

  private fun saveClicked(model: NewMedicalHistoryModel): Next<NewMedicalHistoryModel, NewMedicalHistoryEffect> {
    return when {
      model.facilityDiabetesManagementEnabled && !model.hasAnsweredBothDiagnosisQuestions -> {
        dispatch(ShowDiagnosisRequiredError)
      }
      model.showOngoingHypertensionTreatment && !model.answeredIsOnHypertensionTreatment -> {
        dispatch(ShowOngoingHypertensionTreatmentError)
      }
      else -> {
        next(model.registeringPatient(), RegisterPatient(model.ongoingMedicalHistoryEntry))
      }
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

    return next(updatedModel)
  }

  private fun answerToggled(
      model: NewMedicalHistoryModel,
      answeredQuestion: MedicalHistoryQuestion,
      newAnswer: Answer
  ): Next<NewMedicalHistoryModel, NewMedicalHistoryEffect> {
    return next(model.answerChanged(answeredQuestion, newAnswer))
  }
}
