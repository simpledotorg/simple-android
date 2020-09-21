package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class TeleconsultRecordInit : Init<TeleconsultRecordModel, TeleconsultRecordEffect> {

  override fun init(model: TeleconsultRecordModel): First<TeleconsultRecordModel, TeleconsultRecordEffect> {
    val effects = mutableSetOf<TeleconsultRecordEffect>(LoadTeleconsultRecord(model.teleconsultRecordId))
    if (model.hasPatient.not()) {
      effects.add(LoadPatientDetails(model.patientUuid))
    }
    return first(model, effects)
  }
}
