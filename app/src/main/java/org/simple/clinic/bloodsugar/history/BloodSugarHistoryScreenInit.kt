package org.simple.clinic.bloodsugar.history

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class BloodSugarHistoryScreenInit : Init<BloodSugarHistoryScreenModel, BloodSugarHistoryScreenEffect> {
  override fun init(model: BloodSugarHistoryScreenModel): First<BloodSugarHistoryScreenModel, BloodSugarHistoryScreenEffect> {
    return first(model, LoadPatient(model.patientUuid), ShowBloodSugars(model.patientUuid))
  }
}
