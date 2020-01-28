package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class NewMedicalHistoryUpdate : Update<NewMedicalHistoryModel, NewMedicalHistoryEvent, NewMedicalHistoryEffect> {

  override fun update(model: NewMedicalHistoryModel, event: NewMedicalHistoryEvent): Next<NewMedicalHistoryModel, NewMedicalHistoryEffect> {
    return when (event) {
      is NewMedicalHistoryAnswerToggled -> next(model.answerChanged(event.question, event.answer))
      is SaveMedicalHistoryClicked -> dispatch(RegisterPatient(model.ongoingMedicalHistoryEntry))
      is PatientRegistered -> dispatch(OpenPatientSummaryScreen(event.patientUuid))
      is OngoingPatientEntryLoaded -> next(model.ongoingPatientEntryLoaded(event.ongoingNewPatientEntry))
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility))
    }
  }
}
