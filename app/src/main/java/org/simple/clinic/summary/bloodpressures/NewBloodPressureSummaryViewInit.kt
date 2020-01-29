package org.simple.clinic.summary.bloodpressures

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class NewBloodPressureSummaryViewInit(
    val config: NewBloodPressureSummaryViewConfig
) : Init<NewBloodPressureSummaryViewModel, NewBloodPressureSummaryViewEffect> {
  override fun init(model: NewBloodPressureSummaryViewModel): First<NewBloodPressureSummaryViewModel, NewBloodPressureSummaryViewEffect> {
    return first(model, LoadBloodPressures(model.patientUuid, config.numberOfBpsToDisplay), LoadBloodPressuresCount(model.patientUuid))
  }
}
