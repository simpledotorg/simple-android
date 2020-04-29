package org.simple.clinic.bp.history

import org.simple.clinic.mobius.ViewRenderer

class BloodPressureHistoryScreenUiRenderer(private val ui: BloodPressureHistoryScreenUi) : ViewRenderer<BloodPressureHistoryScreenModel> {

  override fun render(model: BloodPressureHistoryScreenModel) {
    if (model.hasPatient) {
      ui.showPatientInformation(model.patient!!)
    }
  }
}
