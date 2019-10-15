package org.simple.clinic.bp.entry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class BloodPressureEntryInit : Init<BloodPressureEntryModel, BloodPressureEntryEffect> {
  override fun init(
      model: BloodPressureEntryModel
  ): First<BloodPressureEntryModel, BloodPressureEntryEffect> {
    return first(model)
  }
}
