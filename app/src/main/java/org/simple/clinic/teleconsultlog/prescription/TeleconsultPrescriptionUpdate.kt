package org.simple.clinic.teleconsultlog.prescription

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class TeleconsultPrescriptionUpdate : Update<TeleconsultPrescriptionModel, TeleconsultPrescriptionEvent, TeleconsultPrescriptionEffect> {

  override fun update(
      model: TeleconsultPrescriptionModel,
      event: TeleconsultPrescriptionEvent
  ): Next<TeleconsultPrescriptionModel, TeleconsultPrescriptionEffect> {
    return when (event) {
      is PatientDetailsLoaded -> next(model.patientLoaded(event.patient))
      BackClicked -> dispatch(GoBack)
      is DataForNextClickLoaded -> dataForNextClickLoaded(model, event)
      is NextButtonClicked -> dispatch(LoadDataForNextClick(
          teleconsultRecordId = model.teleconsultRecordId,
          medicalInstructions = event.medicalInstructions,
          medicalRegistrationId = event.medicalRegistrationId
      ))
      is TeleconsultIdAddedToPrescribedDrugs -> dispatch(OpenSharePrescriptionScreen(
          teleconsultRecordId = model.teleconsultRecordId,
          medicalInstructions = event.medicalInstructions
      ))
    }
  }

  private fun dataForNextClickLoaded(
      model: TeleconsultPrescriptionModel,
      event: DataForNextClickLoaded
  ): Next<TeleconsultPrescriptionModel, TeleconsultPrescriptionEffect> {
    return if (event.hasSignatureBitmap) {
      dispatch(
          SaveMedicalRegistrationId(medicalRegistrationId = event.medicalRegistrationId),
          UpdateTeleconsultRecordMedicalRegistrationId(
              teleconsultRecordId = model.teleconsultRecordId,
              medicalRegistrationId = event.medicalRegistrationId
          ),
          AddTeleconsultIdToPrescribedDrugs(
              patientUuid = model.patientUuid,
              teleconsultRecordId = model.teleconsultRecordId,
              medicalInstructions = event.medicalInstructions
          )
      )
    } else {
      dispatch(ShowSignatureRequiredError)
    }
  }
}
