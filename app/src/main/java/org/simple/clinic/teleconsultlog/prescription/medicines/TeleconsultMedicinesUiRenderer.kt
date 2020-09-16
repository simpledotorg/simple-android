package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.mobius.ViewRenderer

class TeleconsultMedicinesUiRenderer(
    private val ui: TeleconsultMedicinesUi
) : ViewRenderer<TeleconsultMedicinesModel> {

  override fun render(model: TeleconsultMedicinesModel) {
    if (model.hasMedicines) {
      renderMedicines(model.medicines!!)
    }
  }

  private fun renderMedicines(medicines: List<PrescribedDrug>) {
    if (medicines.isNotEmpty()) {
      ui.renderMedicines(medicines)
    }
  }
}
