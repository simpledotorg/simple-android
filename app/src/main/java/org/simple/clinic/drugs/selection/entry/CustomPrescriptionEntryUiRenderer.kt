package org.simple.clinic.drugs.selection.entry

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class CustomPrescriptionEntryUiRenderer(val ui: CustomPrescriptionEntryUi) : ViewRenderer<CustomPrescriptionEntryModel> {

  private val drugNameValueCallback = ValueChangedCallback<Boolean>()

  override fun render(model: CustomPrescriptionEntryModel) {
    if (model.drugName == null) return

    val isDrugNameNotBlank = model.drugName.isNotBlank()
    drugNameValueCallback.pass(isDrugNameNotBlank, ui::setSaveButtonEnabled)
  }
}
