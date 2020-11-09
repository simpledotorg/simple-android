package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class BloodSugarEntryInit : Init<BloodSugarEntryModel, BloodSugarEntryEffect> {
  override fun init(model: BloodSugarEntryModel): First<BloodSugarEntryModel, BloodSugarEntryEffect> {
    return when (model.openAs) {
      is OpenAs.New -> first(
          model,
          setOf(
              PrefillDate.forNewEntry(),
              LoadBloodSugarUnitPreference
          ))
      is OpenAs.Update -> first(
          model,
          setOf(
              FetchBloodSugarMeasurement(model.openAs.bloodSugarMeasurementUuid) as BloodSugarEntryEffect,
              LoadBloodSugarUnitPreference
          ))
    }
  }
}
