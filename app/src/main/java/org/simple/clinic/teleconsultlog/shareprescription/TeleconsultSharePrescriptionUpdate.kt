package org.simple.clinic.teleconsultlog.shareprescription

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class TeleconsultSharePrescriptionUpdate : Update<TeleconsultSharePrescriptionModel, TeleconsultSharePrescriptionEvent, TeleconsultSharePrescriptionEffect> {
  override fun update(
      model: TeleconsultSharePrescriptionModel,
      event: TeleconsultSharePrescriptionEvent
  ): Next<TeleconsultSharePrescriptionModel, TeleconsultSharePrescriptionEffect> {
    return when (event) {
      is PatientDetailsLoaded -> next(model.patientLoaded(event.patient))
      is PatientMedicinesLoaded -> next(model.patientMedicinesLoaded(event.medicines))
      is SignatureLoaded -> dispatch(SetSignature(event.bitmap))
      is MedicalRegistrationIdLoaded -> noChange()
    }
  }
}
