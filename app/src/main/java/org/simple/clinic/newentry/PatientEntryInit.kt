package org.simple.clinic.newentry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class PatientEntryInit : Init<PatientEntryModel, PatientEntryEffect> {
  override fun init(model: PatientEntryModel): First<PatientEntryModel, PatientEntryEffect> {
    val effects = mutableSetOf<PatientEntryEffect>(LoadInputFields)

    // The ongoing entry is only fetched the first time. When the screen is restored, the model
    // already has the entry, including changes like an added SHR ID that are not in the
    // repository, so fetching it again would overwrite them.
    if (!model.isPatientEntryFetched) {
      effects.add(FetchPatientEntry)
    }

    if (!model.hasColonyOrVillagesList) {
      effects.add(FetchColonyOrVillagesEffect)
    }

    return first(model, effects)
  }
}
