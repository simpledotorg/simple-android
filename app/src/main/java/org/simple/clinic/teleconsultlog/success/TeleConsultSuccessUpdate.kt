package org.simple.clinic.teleconsultlog.success

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class TeleConsultSuccessUpdate : Update<TeleConsultSuccessModel, TeleConsultSuccessEvent, TeleConsultSuccessEffect> {
  override fun update(model: TeleConsultSuccessModel, event: TeleConsultSuccessEvent): Next<TeleConsultSuccessModel, TeleConsultSuccessEffect> {
    return when (event) {
      is PatientDetailsLoaded -> next(model.patientDetailLoaded(event.patient))
    }
  }
}
