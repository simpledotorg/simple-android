package org.simple.clinic.teleconsultlog.prescription.patientinfo

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class TeleconsultPatientInfoUpdate : Update<TeleconsultPatientInfoModel, TeleconsultPatientInfoEvent, TeleconsultPatientInfoEffect> {

  override fun update(
      model: TeleconsultPatientInfoModel,
      event: TeleconsultPatientInfoEvent
  ): Next<TeleconsultPatientInfoModel, TeleconsultPatientInfoEffect> {
    return when (event) {
      is PatientProfileLoaded -> next(model.patientProfileLoaded(event.patientProfile))
    }
  }
}
