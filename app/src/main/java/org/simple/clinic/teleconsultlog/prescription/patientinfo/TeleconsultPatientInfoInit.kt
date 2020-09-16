package org.simple.clinic.teleconsultlog.prescription.patientinfo

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class TeleconsultPatientInfoInit : Init<TeleconsultPatientInfoModel, TeleconsultPatientInfoEffect> {

  override fun init(model: TeleconsultPatientInfoModel): First<TeleconsultPatientInfoModel, TeleconsultPatientInfoEffect> {
    val effects = mutableSetOf<TeleconsultPatientInfoEffect>()
    if (model.hasPatientProfile.not()) {
      effects.add(LoadPatientProfile(model.patientUuid))
    }

    return first(model, effects)
  }
}
