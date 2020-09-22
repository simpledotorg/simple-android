package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import org.simple.clinic.mobius.ViewRenderer

class TeleconsultDoctorInfoUiRenderer(val ui: TeleconsultDoctorInfoUi) : ViewRenderer<TeleconsultDoctorInfoModel> {

  override fun render(model: TeleconsultDoctorInfoModel) {
    if (model.hasUser) {
      ui.renderDoctorAcknowledgement(model.user!!)
    }
  }
}
