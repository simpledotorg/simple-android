package org.simple.clinic.bp.entry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.bp.entry.OpenAs.Update

class BloodPressureEntryInit : Init<BloodPressureEntryModel, BloodPressureEntryEffect> {
  override fun init(
      model: BloodPressureEntryModel
  ): First<BloodPressureEntryModel, BloodPressureEntryEffect> {
    return when {
      model.openAs is New -> first(model, setOf(PrefillDate.forNewEntry()))
      model.openAs is Update -> first(model, setOf(FetchBloodPressureMeasurement(model.openAs.bpUuid) as BloodPressureEntryEffect))
      else -> first(model)
    }
  }
}
