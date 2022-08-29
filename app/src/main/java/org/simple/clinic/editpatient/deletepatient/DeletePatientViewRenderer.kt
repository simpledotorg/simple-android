package org.simple.clinic.editpatient.deletepatient

import org.simple.clinic.mobius.ViewRenderer

class DeletePatientViewRenderer(
    private val ui: DeletePatientUi
) : ViewRenderer<DeletePatientModel> {

  private val patientDeleteReasons = listOf(
      PatientDeleteReason.Duplicate,
      PatientDeleteReason.AccidentalRegistration,
      PatientDeleteReason.Died
  )

  override fun render(model: DeletePatientModel) {
    if (model.hasPatientName) {
      ui.showDeleteReasons(patientDeleteReasons = patientDeleteReasons, selectedReason = model.selectedReason)
    }
  }
}
