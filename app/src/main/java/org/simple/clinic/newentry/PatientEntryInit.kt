package org.simple.clinic.newentry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class PatientEntryInit(private val isVillageTypeAheadEnabled: Boolean) : Init<PatientEntryModel, PatientEntryEffect> {
  override fun init(model: PatientEntryModel): First<PatientEntryModel, PatientEntryEffect> {
    val effects = mutableSetOf(FetchPatientEntry, LoadInputFields)

    if (!model.hasColonyOrVillagesList && isVillageTypeAheadEnabled) {
      effects.add(FetchColonyOrVillagesEffect)
    }

    return first(model, effects)
  }
}
