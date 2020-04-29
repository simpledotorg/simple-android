package org.simple.clinic.bp.history

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class BloodPressureHistoryScreenInit : Init<BloodPressureHistoryScreenModel, BloodPressureHistoryScreenEffect> {
  override fun init(model: BloodPressureHistoryScreenModel): First<BloodPressureHistoryScreenModel, BloodPressureHistoryScreenEffect> {
    return if (model.hasPatient) {
      first(model)
    } else {
      first(model, LoadPatient(model.patientUuid), ShowBloodPressures(model.patientUuid))
    }
  }
}
