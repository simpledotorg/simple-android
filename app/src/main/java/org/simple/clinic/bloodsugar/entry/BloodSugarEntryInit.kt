package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class BloodSugarEntryInit : Init<BloodSugarEntryModel, BloodSugarEntryEffect> {
  override fun init(model: BloodSugarEntryModel): First<BloodSugarEntryModel, BloodSugarEntryEffect> {
    return first(model, setOf(PrefillDate.forNewEntry()))
  }
}
