package org.simple.clinic.teleconsultlog.shareprescription

import com.spotify.mobius.First
import com.spotify.mobius.Init

class TeleconsultSharePrescriptionInit : Init<TeleconsultSharePrescriptionModel, TeleconsultSharePrescriptionEffect> {

  override fun init(model: TeleconsultSharePrescriptionModel): First<TeleconsultSharePrescriptionModel, TeleconsultSharePrescriptionEffect> {
    val effects = mutableSetOf<TeleconsultSharePrescriptionEffect>()

    if (model.hasPatientProfile.not())
      effects.add(LoadPatientProfile(model.patientUuid))

    if (model.hasMedicines.not())
      effects.add(LoadPatientMedicines(model.patientUuid))

    if (model.hasMedicalRegistrationId.not())
      effects.add(LoadMedicalRegistrationId)

    effects.add(LoadSignature)

    return First.first(model, effects)
  }
}
