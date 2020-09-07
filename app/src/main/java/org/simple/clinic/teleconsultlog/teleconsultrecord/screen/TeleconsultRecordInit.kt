package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class TeleconsultRecordInit : Init<TeleconsultRecordModel, TeleconsultRecordEffect> {

  override fun init(model: TeleconsultRecordModel): First<TeleconsultRecordModel, TeleconsultRecordEffect> {
    return first(model, LoadTeleconsultRecordWithPrescribedDrugs(model.teleconsultRecordId))
  }
}
