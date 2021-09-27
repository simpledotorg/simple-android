package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency

class TeleconsultMedicinesUiRenderer(
    private val ui: TeleconsultMedicinesUi
) : ViewRenderer<TeleconsultMedicinesModel> {

  override fun render(model: TeleconsultMedicinesModel) {
    if (model.hasMedicines) {
      renderMedicines(
          model.medicines!!,
          model.hasMedicineFrequencyToLabelMap)
    }
  }

  private fun renderMedicines(
      medicines: List<PrescribedDrug>,
      hasMedicineFrequencyToFrequencyChoiceItemMap: Boolean
  ) {
    if (medicines.isNotEmpty() && hasMedicineFrequencyToFrequencyChoiceItemMap) {
      ui.renderMedicines(medicines)
      ui.showEditButton()
      ui.hideMedicinesRequiredError()
    } else {
      ui.showNoMedicines()
      ui.showAddButton()
    }
  }
}
