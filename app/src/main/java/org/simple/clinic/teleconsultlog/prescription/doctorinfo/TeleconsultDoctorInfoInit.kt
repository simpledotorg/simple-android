package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class TeleconsultDoctorInfoInit : Init<TeleconsultDoctorInfoModel, TeleconsultDoctorInfoEffect> {

  override fun init(model: TeleconsultDoctorInfoModel): First<TeleconsultDoctorInfoModel, TeleconsultDoctorInfoEffect> {
    return first(model, LoadMedicalRegistrationId)
  }
}
