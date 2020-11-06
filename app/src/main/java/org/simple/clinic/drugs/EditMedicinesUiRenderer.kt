package org.simple.clinic.drugs

import org.simple.clinic.drugs.selection.EditMedicinesUi
import org.simple.clinic.drugs.selection.ProtocolDrugListItem
import org.simple.clinic.drugs.selection.entry.CustomPrescribedDrugListItem
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.protocol.ProtocolDrugAndDosages

class EditMedicinesUiRenderer(private val ui: EditMedicinesUi) : ViewRenderer<EditMedicinesModel> {

  override fun render(model: EditMedicinesModel) {
    if (model.prescribedDrugs != null && model.protocolDrugs != null)
      renderPrescribedProtocolDrugs(model, model.prescribedDrugs, model.protocolDrugs)
    ui.showDoneButton()
    ui.hideRefillMedicineButton()
  }

  private fun renderPrescribedProtocolDrugs(
      model: EditMedicinesModel,
      prescribedDrugs: List<PrescribedDrug>,
      protocolDrugs: List<ProtocolDrugAndDosages>
  ) {
    val (prescribedProtocolDrugs, prescribedCustomDrugs) = prescribedDrugs.partition(model::isProtocolDrug)
    val isAtLeastOneCustomDrugPrescribed = prescribedCustomDrugs.isNotEmpty()

    val protocolDrugSelectionItems = protocolDrugSelectionItems(protocolDrugs, prescribedProtocolDrugs, isAtLeastOneCustomDrugPrescribed)
    val customPrescribedDrugItems = customPrescribedDrugItems(prescribedCustomDrugs)
    val drugsList = protocolDrugSelectionItems + customPrescribedDrugItems

    ui.populateDrugsList(drugsList)
  }

  private fun customPrescribedDrugItems(
      prescribedCustomDrugs: List<PrescribedDrug>
  ): List<CustomPrescribedDrugListItem> {
    return prescribedCustomDrugs
        .sortedBy { it.updatedAt }
        .mapIndexed { index, prescribedDrug -> CustomPrescribedDrugListItem(prescribedDrug, index == prescribedCustomDrugs.lastIndex) }
  }

  private fun protocolDrugSelectionItems(
      protocolDrugs: List<ProtocolDrugAndDosages>,
      prescribedProtocolDrugs: List<PrescribedDrug>,
      isAtLeastOneCustomDrugPrescribed: Boolean
  ): List<ProtocolDrugListItem> {
    // Show dosage if prescriptions exist for them.
    return protocolDrugs
        .mapIndexed { index: Int, drugAndDosages: ProtocolDrugAndDosages ->
          val matchingPrescribedDrug = prescribedProtocolDrugs.firstOrNull { it.name == drugAndDosages.drugName }
          ProtocolDrugListItem(
              id = index,
              drugName = drugAndDosages.drugName,
              prescribedDrug = matchingPrescribedDrug,
              hideDivider = isAtLeastOneCustomDrugPrescribed.not() && index == protocolDrugs.lastIndex)
        }
  }
}
