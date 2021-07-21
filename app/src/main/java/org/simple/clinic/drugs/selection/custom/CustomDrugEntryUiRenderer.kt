package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class CustomDrugEntryUiRenderer(
    private val ui: CustomDrugEntryUi
) : ViewRenderer<CustomDrugEntryModel> {
  private val drugFrequencyValueCallBack = ValueChangedCallback<DrugFrequency>()

  override fun render(model: CustomDrugEntryModel) {
    if(model.hasDrugFrequency){
      drugFrequencyValueCallBack.pass(model.frequency!!, ui::setDrugFrequency)
    }
  }
}
