package org.simple.clinic.patientcontact

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class PatientContactInit : Init<PatientContactModel, PatientContactEffect> {

  override fun init(
      model: PatientContactModel
  ): First<PatientContactModel, PatientContactEffect> {
    val effects = mutableSetOf<PatientContactEffect>()

    if (!model.hasLoadedPatientProfile) {
      effects.add(LoadPatient(model.patientUuid))
    }

    if (!model.hasLoadedAppointment) {
      effects.add(LoadLatestOverdueAppointment(model.patientUuid))
    }

    return first(model, effects)
  }
}
