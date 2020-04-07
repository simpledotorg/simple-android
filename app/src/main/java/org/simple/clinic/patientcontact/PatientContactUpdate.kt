package org.simple.clinic.patientcontact

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class PatientContactUpdate : Update<PatientContactModel, PatientContactEvent, PatientContactEffect> {

  override fun update(
      model: PatientContactModel,
      event: PatientContactEvent
  ): Next<PatientContactModel, PatientContactEffect> {
    return when(event) {
      is PatientProfileLoaded -> next(model.patientProfileLoaded(event.patientProfile))
    }
  }
}
