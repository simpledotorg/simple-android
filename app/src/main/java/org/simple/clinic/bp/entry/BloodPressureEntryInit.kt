package org.simple.clinic.bp.entry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.bp.entry.OpenAs.New

class BloodPressureEntryInit : Init<BloodPressureEntryModel, BloodPressureEntryEffect> {
  override fun init(
      model: BloodPressureEntryModel
  ): First<BloodPressureEntryModel, BloodPressureEntryEffect> {
    return if (model.openAs is New) {
      first(model, setOf(PrefillDateForNewEntry))
    } else {
      first(model)
    }
  }
}
