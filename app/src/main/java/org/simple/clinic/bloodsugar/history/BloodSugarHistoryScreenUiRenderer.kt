package org.simple.clinic.bloodsugar.history

import org.simple.clinic.mobius.ViewRenderer

class BloodSugarHistoryScreenUiRenderer(private val ui: BloodSugarHistoryScreenUi) : ViewRenderer<BloodSugarHistoryScreenModel> {
  override fun render(model: BloodSugarHistoryScreenModel) {
    if (model.hasLoadedPatient) {
      ui.showPatientInformation(model.patient!!)
    }
  }
}
