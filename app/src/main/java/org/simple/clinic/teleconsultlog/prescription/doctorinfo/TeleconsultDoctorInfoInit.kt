package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class TeleconsultDoctorInfoInit : Init<TeleconsultDoctorInfoModel, TeleconsultDoctorInfoEffect> {

  override fun init(model: TeleconsultDoctorInfoModel): First<TeleconsultDoctorInfoModel, TeleconsultDoctorInfoEffect> {
    val effects = mutableSetOf<TeleconsultDoctorInfoEffect>()
    if (model.hasMedicalRegistrationId) {
      effects.add(SetMedicalRegistrationId(model.medicalRegistrationId!!))
    } else {
      effects.add(LoadMedicalRegistrationId)
    }

    return first(model, effects)
  }
}
