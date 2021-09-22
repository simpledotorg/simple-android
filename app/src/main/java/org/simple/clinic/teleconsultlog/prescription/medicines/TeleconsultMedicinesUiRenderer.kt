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
          model.hasMedicineFrequencyToLabelMap,
          model.medicineFrequencyToLabelMap)
    }
  }

  private fun renderMedicines(
      medicines: List<PrescribedDrug>,
      hasMedicineFrequencyToFrequencyChoiceItemMap: Boolean,
      medicineFrequencyToLabelMap: Map<MedicineFrequency?, DrugFrequencyLabel>?
  ) {
    if (medicines.isNotEmpty() && hasMedicineFrequencyToFrequencyChoiceItemMap) {
      ui.renderMedicines(medicines, medicineFrequencyToLabelMap!!)
      ui.showEditButton()
      ui.hideMedicinesRequiredError()
    } else {
      ui.showNoMedicines()
      ui.showAddButton()
    }
  }
}
