package org.simple.clinic.summary.teleconsultation.contactdoctor

import org.simple.clinic.mobius.ViewRenderer

class ContactDoctorUiRenderer(
    private val ui: ContactDoctorUi
) : ViewRenderer<ContactDoctorModel> {

  override fun render(model: ContactDoctorModel) {
    if (model.hasMedicalOfficers) {
      ui.showMedicalOfficers(model.medicalOfficers!!)
    }
  }
}
