package org.simple.clinic.teleconsultlog.shareprescription

import com.spotify.mobius.First
import com.spotify.mobius.Init

class TeleconsultSharePrescriptionInit : Init<TeleconsultSharePrescriptionModel, TeleconsultSharePrescriptionEffect> {

  override fun init(model: TeleconsultSharePrescriptionModel): First<TeleconsultSharePrescriptionModel, TeleconsultSharePrescriptionEffect> {
    val effects = mutableSetOf<TeleconsultSharePrescriptionEffect>()

    if (model.hasPatientProfile.not()) {
      effects.addAll(listOf(
          LoadPatientProfile(model.patientUuid),
          LoadPatientMedicines(model.patientUuid),
          LoadSignature,
          LoadMedicalRegistrationId))
    }

    return First.first(model, effects)
  }
}
