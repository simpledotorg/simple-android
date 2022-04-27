package org.simple.clinic.drugstockreminders.enterdrugstock

import org.simple.clinic.mobius.ViewRenderer

class EnterDrugStockScreenUiRenderer(
    private val ui: EnterDrugStockUi
) : ViewRenderer<EnterDrugStockModel> {

  override fun render(model: EnterDrugStockModel) {
    if (model.isDrugStockUrlLoaded)
      ui.loadDrugStockForm(model.drugStockFormUrl!!)
  }
}
