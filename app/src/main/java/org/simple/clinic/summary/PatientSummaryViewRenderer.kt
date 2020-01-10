package org.simple.clinic.summary

import org.simple.clinic.mobius.ViewRenderer

class PatientSummaryViewRenderer(
    private val ui: PatientSummaryScreenUi
) : ViewRenderer<PatientSummaryModel> {

  override fun render(model: PatientSummaryModel) {
    with(ui) {
      if (model.patientSummaryProfile != null) {
        populatePatientProfile(model.patientSummaryProfile)
        showEditButton()
      }
    }
  }
}
