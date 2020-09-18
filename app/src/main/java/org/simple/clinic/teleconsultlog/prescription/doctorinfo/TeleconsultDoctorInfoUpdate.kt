package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class TeleconsultDoctorInfoUpdate : Update<TeleconsultDoctorInfoModel, TeleconsultDoctorInfoEvent, TeleconsultDoctorInfoEffect> {

  override fun update(
      model: TeleconsultDoctorInfoModel,
      event: TeleconsultDoctorInfoEvent
  ): Next<TeleconsultDoctorInfoModel, TeleconsultDoctorInfoEffect> {
    return when (event) {
      is MedicalRegistrationIdLoaded -> medicalRegistrationIdLoaded(model, event)
    }
  }

  private fun medicalRegistrationIdLoaded(
      model: TeleconsultDoctorInfoModel,
      event: MedicalRegistrationIdLoaded
  ): Next<TeleconsultDoctorInfoModel, TeleconsultDoctorInfoEffect> {
    return next(model.medicalRegistrationIdLoaded(event.medicalRegistrationId), SetMedicalRegistrationId(event.medicalRegistrationId))
  }
}
