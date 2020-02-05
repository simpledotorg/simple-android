package org.simple.clinic.summary.bloodpressures

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class BloodPressureSummaryViewInit(
    private val config: BloodPressureSummaryViewConfig
) : Init<BloodPressureSummaryViewModel, BloodPressureSummaryViewEffect> {
  override fun init(model: BloodPressureSummaryViewModel): First<BloodPressureSummaryViewModel, BloodPressureSummaryViewEffect> {
    val effects = mutableSetOf<BloodPressureSummaryViewEffect>()

    effects.add(LoadBloodPressuresCount(model.patientUuid))

    if (!model.hasLoadedFacility) {
      effects.add(LoadCurrentFacility)
    } else {
      val numberOfBpsToDisplay = if (model.isDiabetesManagementEnabled) {
        config.numberOfBpsToDisplay
      } else {
        config.numberOfBpsToDisplayWithoutDiabetesManagement
      }

      effects.add(LoadBloodPressures(model.patientUuid, numberOfBpsToDisplay))
    }

    return first(model, effects)
  }
}
