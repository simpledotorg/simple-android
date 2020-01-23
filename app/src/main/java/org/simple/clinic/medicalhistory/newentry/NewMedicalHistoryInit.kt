package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class NewMedicalHistoryInit : Init<NewMedicalHistoryModel, NewMedicalHistoryEffect> {

  override fun init(model: NewMedicalHistoryModel): First<NewMedicalHistoryModel, NewMedicalHistoryEffect> {
    return first(model)
  }
}
