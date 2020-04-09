package org.simple.clinic.summary.bloodsugar

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class BloodSugarSummaryViewInit : Init<BloodSugarSummaryViewModel, BloodSugarSummaryViewEffect> {

  override fun init(model: BloodSugarSummaryViewModel): First<BloodSugarSummaryViewModel, BloodSugarSummaryViewEffect> {
    return first(model, FetchBloodSugarSummary(model.patientUuid), FetchBloodSugarCount(model.patientUuid))
  }
}
