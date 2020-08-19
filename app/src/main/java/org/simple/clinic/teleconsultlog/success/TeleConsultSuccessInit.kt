package org.simple.clinic.teleconsultlog.success

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessEffect.LoadPatientDetails

class TeleConsultSuccessInit : Init<TeleConsultSuccessModel, TeleConsultSuccessEffect> {
  override fun init(model: TeleConsultSuccessModel): First<TeleConsultSuccessModel, TeleConsultSuccessEffect> {
    return if (model.hasPatient.not())
      first(model, LoadPatientDetails(model.patientUuid))
    else
      first(model)
  }
}
