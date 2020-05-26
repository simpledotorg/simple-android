package org.simple.clinic.editpatient.deletepatient

class DeletePatientViewRenderer(
    private val ui: DeletePatientUi
) {

  private val patientDeleteReasons = listOf(
      PatientDeleteReason.Duplicate,
      PatientDeleteReason.AccidentalRegistration,
      PatientDeleteReason.Died
  )

  fun render(model: DeletePatientModel) {
    if (model.hasPatientName) {
      ui.showDeleteReasons(patientDeleteReasons = patientDeleteReasons, selectedReason = model.selectedReason)
    }
  }
}
