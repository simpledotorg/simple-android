package org.simple.clinic.teleconsultlog.shareprescription

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class TeleconsultSharePrescriptionUpdate : Update<TeleconsultSharePrescriptionModel, TeleconsultSharePrescriptionEvent, TeleconsultSharePrescriptionEffect> {
  override fun update(
      model: TeleconsultSharePrescriptionModel,
      event: TeleconsultSharePrescriptionEvent
  ): Next<TeleconsultSharePrescriptionModel, TeleconsultSharePrescriptionEffect> {
    return when (event) {
      is PatientMedicinesLoaded -> next(model.patientMedicinesLoaded(event.medicines))
      is SignatureLoaded -> dispatch(SetSignature(event.bitmap))
      is MedicalRegistrationIdLoaded -> next(model.medicalRegistrationIdLoaded(event.medicalRegistrationId), SetMedicalRegistrationId(event.medicalRegistrationId))
      is DownloadClicked -> dispatch(SaveBitmapInExternalStorage(event.bitmap))
      PrescriptionImageSaved -> dispatch(ShowImageSavedToast)
      DoneClicked -> dispatch(GoToHomeScreen)
      is PatientProfileLoaded -> next(model.patientProfileLoaded(event.patientProfile))
      is ShareClicked -> dispatch(SharePrescriptionAsImage(event.bitmap))
      is PrescriptionSavedForSharing -> dispatch(RetrievePrescriptionImageUri(event.fileName))
      is SharePrescriptionUri -> dispatch(OpenSharingDialog(event.imageUri))
      BackClicked -> dispatch(GoBack)
      ImageSavedMessageShown -> dispatch(GoToHomeScreen)
    }
  }
}
