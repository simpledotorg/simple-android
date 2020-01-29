package org.simple.clinic.summary.bloodpressures

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class BloodPressureSummaryViewInit(
    val config: BloodPressureSummaryViewConfig
) : Init<BloodPressureSummaryViewModel, BloodPressureSummaryViewEffect> {
  override fun init(model: BloodPressureSummaryViewModel): First<BloodPressureSummaryViewModel, BloodPressureSummaryViewEffect> {
    return first(model, LoadBloodPressures(model.patientUuid, config.numberOfBpsToDisplay), LoadBloodPressuresCount(model.patientUuid))
  }
}
