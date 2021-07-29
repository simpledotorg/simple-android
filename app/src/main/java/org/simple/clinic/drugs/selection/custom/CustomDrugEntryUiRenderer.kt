package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class CustomDrugEntryUiRenderer(
    private val ui: CustomDrugEntryUi
) : ViewRenderer<CustomDrugEntryModel> {
  private val drugFrequencyValueCallBack = ValueChangedCallback<DrugFrequency>()
  private val drugDosageValueCallBack = ValueChangedCallback<String>()
  override fun render(model: CustomDrugEntryModel) {
    initialSetup()

    ui.setDrugName(model.drugName)

    if (model.hasDrugFrequency) {
      drugFrequencyValueCallBack.pass(model.frequency!!, ui::setDrugFrequency)
    }

    if (model.hasDrugDosage) {
      drugDosageValueCallBack.pass(model.dosage!!, ui::setDrugDosage)
    }
  }

  private fun initialSetup() {
      setUpUIForCreatingDrugEntry()
    }

  fun setUpUIForCreatingDrugEntry() {
    ui.hideRemoveButton()
    ui.setButtonTextAsAdd()
  }
}
