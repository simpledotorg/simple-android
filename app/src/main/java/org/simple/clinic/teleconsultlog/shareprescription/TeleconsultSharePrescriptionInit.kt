package org.simple.clinic.teleconsultlog.shareprescription

import com.spotify.mobius.First
import com.spotify.mobius.Init

class TeleconsultSharePrescriptionInit : Init<TeleconsultSharePrescriptionModel, TeleconsultSharePrescriptionEffect> {

  override fun init(model: TeleconsultSharePrescriptionModel): First<TeleconsultSharePrescriptionModel, TeleconsultSharePrescriptionEffect> {
    val effects = mutableSetOf<TeleconsultSharePrescriptionEffect>()

    if (model.hasPatientProfile.not()) {
      effects.add(LoadPatientProfile(model.patientUuid))
      if (model.hasPatient.not()) {
        effects.addAll(listOf(
            LoadPatientDetails(model.patientUuid),
            LoadPatientMedicines(model.patientUuid),
            LoadSignature,
            LoadMedicalRegistrationId))
      }
    }

    return First.first(model, effects)
  }
}
