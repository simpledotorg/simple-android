package org.simple.clinic.teleconsultlog.prescription

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class TeleconsultPrescriptionInit : Init<TeleconsultPrescriptionModel, TeleconsultPrescriptionEffect> {

  override fun init(model: TeleconsultPrescriptionModel): First<TeleconsultPrescriptionModel, TeleconsultPrescriptionEffect> {
    val effects = mutableSetOf<TeleconsultPrescriptionEffect>()
    if (model.hasPatient.not()) {
      effects.add(LoadPatientDetails(model.patientUuid))
    }
    return first(model, effects)
  }
}
