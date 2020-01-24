package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class NewMedicalHistoryInit : Init<NewMedicalHistoryModel, NewMedicalHistoryEffect> {

  override fun init(model: NewMedicalHistoryModel): First<NewMedicalHistoryModel, NewMedicalHistoryEffect> {
    val effects = if(model.ongoingPatientEntry != null) emptySet() else setOf(LoadOngoingPatientEntry)
    return first(model, effects)
  }
}
