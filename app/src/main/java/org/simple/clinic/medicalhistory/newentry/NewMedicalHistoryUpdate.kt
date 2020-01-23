package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class NewMedicalHistoryUpdate : Update<NewMedicalHistoryModel, NewMedicalHistoryEvent, NewMedicalHistoryEffect> {

  override fun update(model: NewMedicalHistoryModel, event: NewMedicalHistoryEvent): Next<NewMedicalHistoryModel, NewMedicalHistoryEffect> {
    return noChange()
  }
}
