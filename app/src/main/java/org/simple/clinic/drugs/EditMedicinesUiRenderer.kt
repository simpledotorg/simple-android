package org.simple.clinic.drugs

import org.simple.clinic.drugs.selection.PrescribedDrugUi
import org.simple.clinic.drugs.selection.ProtocolDrugListItem
import org.simple.clinic.drugs.selection.entry.CustomPrescribedDrugListItem
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.protocol.ProtocolDrugAndDosages

class EditMedicinesUiRenderer(private val ui: PrescribedDrugUi) : ViewRenderer<EditMedicinesModel> {
  override fun render(model: EditMedicinesModel) {
    if (model.prescribedDrugs == null || model.protocolDrugs == null)
      return

    if (model.prescribedDrugs != null && model.protocolDrugs != null)
      renderPrescribedProtocolDrugs(model.prescribedDrugs, model.protocolDrugs)
  }

  private fun renderPrescribedProtocolDrugs(prescribedDrugs: List<PrescribedDrug>, protocolDrugs: List<ProtocolDrugAndDosages>) {
    val prescribedProtocolDrugs = prescribedDrugs.filter { it.isProtocolDrug }
    val isAtLeastOneCustomDrugPrescribed = prescribedDrugs.any { it.isProtocolDrug.not() }
    // Show dosage if prescriptions exist for them.
    val protocolDrugSelectionItems = protocolDrugs
        .mapIndexed { index: Int, drugAndDosages: ProtocolDrugAndDosages ->
          val matchingPrescribedDrug = prescribedProtocolDrugs.firstOrNull { it.name == drugAndDosages.drugName }
          ProtocolDrugListItem(
              id = index,
              drugName = drugAndDosages.drugName,
              prescribedDrug = matchingPrescribedDrug,
              hideDivider = isAtLeastOneCustomDrugPrescribed.not() && index == protocolDrugs.lastIndex)
        }

    val customDrugs = prescribedDrugs
        .filter { it.isProtocolDrug.not() }
    val customPrescribedDrugItems = customDrugs
        .sortedBy { it.updatedAt.toEpochMilli() }
        .mapIndexed { index, prescribedDrug -> CustomPrescribedDrugListItem(prescribedDrug, index == customDrugs.lastIndex) }

    val drugsList = protocolDrugSelectionItems + customPrescribedDrugItems

    ui.populateDrugsList(drugsList)
  }
}
