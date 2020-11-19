package org.simple.clinic.bloodsugar.unitselection

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class BloodSugarUnitSelectionInit : Init<BloodSugarUnitSelectionModel, BloodSugarUnitSelectionEffect> {

  override fun init(model: BloodSugarUnitSelectionModel): First<BloodSugarUnitSelectionModel, BloodSugarUnitSelectionEffect> {
    return first(model, PreFillBloodSugarUnitSelected(model.bloodSugarUnitPreference))
  }
}
