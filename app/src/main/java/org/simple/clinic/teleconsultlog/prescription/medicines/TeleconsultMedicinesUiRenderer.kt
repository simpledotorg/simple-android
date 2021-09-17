package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyChoiceItem
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency

class TeleconsultMedicinesUiRenderer(
    private val ui: TeleconsultMedicinesUi
) : ViewRenderer<TeleconsultMedicinesModel> {

  override fun render(model: TeleconsultMedicinesModel) {
    if (model.hasMedicines) {
      renderMedicines(
          model.medicines!!,
          model.hasMedicineFrequencyToFrequencyChoiceItemMap,
          model.medicineFrequencyToFrequencyChoiceItemMap)
    }
  }

  private fun renderMedicines(
      medicines: List<PrescribedDrug>,
      hasMedicineFrequencyToFrequencyChoiceItemMap: Boolean,
      medicineFrequencyToFrequencyChoiceItemMap: Map<MedicineFrequency?, DrugFrequencyChoiceItem>?
  ) {
    if (medicines.isNotEmpty() && hasMedicineFrequencyToFrequencyChoiceItemMap) {
      ui.renderMedicines(medicines, medicineFrequencyToFrequencyChoiceItemMap!!)
      ui.showEditButton()
      ui.hideMedicinesRequiredError()
    } else {
      ui.showNoMedicines()
      ui.showAddButton()
    }
  }
}
