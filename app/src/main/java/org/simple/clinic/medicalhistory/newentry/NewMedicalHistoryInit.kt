package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class NewMedicalHistoryInit : Init<NewMedicalHistoryModel, NewMedicalHistoryEffect> {

  override fun init(model: NewMedicalHistoryModel): First<NewMedicalHistoryModel, NewMedicalHistoryEffect> {
    val effects = when {
      model.hasNotInitialized -> setOf(LoadOngoingPatientEntry, LoadCurrentFacility)
      model.hasLoadedPatientEntry.not() -> setOf(LoadOngoingPatientEntry)
      model.hasLoadedCurrentFacility.not() -> setOf(LoadCurrentFacility)
      else -> emptySet()
    }

    return first(model, effects)
  }
}
