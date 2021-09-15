package org.simple.clinic.drugs

import org.simple.clinic.drugs.EditMedicineButtonState.REFILL_MEDICINE
import org.simple.clinic.drugs.EditMedicineButtonState.SAVE_MEDICINE
import org.simple.clinic.drugs.selection.CustomPrescribedDrugListItem
import org.simple.clinic.drugs.selection.EditMedicinesUi
import org.simple.clinic.drugs.selection.ProtocolDrugListItem
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyChoiceItem
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency

class EditMedicinesUiRenderer(private val ui: EditMedicinesUi) : ViewRenderer<EditMedicinesModel> {

  override fun render(model: EditMedicinesModel) {
    if (model.hasPrescribedAndProtocolDrugs && model.hasMedicineFrequencyToFrequencyChoiceItemMap)
      renderPrescribedProtocolDrugs(model, model.prescribedDrugs!!, model.protocolDrugs!!, model.medicineFrequencyToFrequencyChoiceItemMap!!)
    when (model.editMedicineButtonState) {
      SAVE_MEDICINE -> {
        ui.showDoneButton()
        ui.hideRefillMedicineButton()
      }
      REFILL_MEDICINE -> {
        ui.showRefillMedicineButton()
        ui.hideDoneButton()
      }
    }
  }

  private fun renderPrescribedProtocolDrugs(
      model: EditMedicinesModel,
      prescribedDrugs: List<PrescribedDrug>,
      protocolDrugs: List<ProtocolDrugAndDosages>,
      medicineFrequencyToFrequencyChoiceItemMap: Map<MedicineFrequency?, DrugFrequencyChoiceItem>
  ) {
    val (prescribedProtocolDrugs, prescribedCustomDrugs) = prescribedDrugs.partition(model::isProtocolDrug)

    val protocolDrugSelectionItems = protocolDrugSelectionItems(protocolDrugs, prescribedProtocolDrugs, medicineFrequencyToFrequencyChoiceItemMap)
    val customPrescribedDrugItems = customPrescribedDrugItems(prescribedCustomDrugs, medicineFrequencyToFrequencyChoiceItemMap)
    val drugsList = (protocolDrugSelectionItems + customPrescribedDrugItems)
        .sortedByDescending { it.prescribedDrug?.updatedAt }
        .mapIndexed { index, drugListItem ->
          val isTopItem = index == 0
          when {
            isTopItem && drugListItem is ProtocolDrugListItem -> drugListItem.withTopCorners()
            isTopItem && drugListItem is CustomPrescribedDrugListItem -> drugListItem.withTopCorners()
            else -> drugListItem
          }
        }

    ui.populateDrugsList(drugsList)
  }

  private fun customPrescribedDrugItems(
      prescribedCustomDrugs: List<PrescribedDrug>,
      medicineFrequencyToFrequencyChoiceItemMap: Map<MedicineFrequency?, DrugFrequencyChoiceItem>
  ): List<CustomPrescribedDrugListItem> {
    return prescribedCustomDrugs
        .map { prescribedDrug ->
          CustomPrescribedDrugListItem(
              prescribedDrug = prescribedDrug,
              hasTopCorners = false,
              medicineFrequencyToFrequencyChoiceItemMap = medicineFrequencyToFrequencyChoiceItemMap
          )
        }
  }

  private fun protocolDrugSelectionItems(
      protocolDrugs: List<ProtocolDrugAndDosages>,
      prescribedProtocolDrugs: List<PrescribedDrug>,
      medicineFrequencyToFrequencyChoiceItemMap: Map<MedicineFrequency?, DrugFrequencyChoiceItem>
  ): List<ProtocolDrugListItem> {
    // Show dosage if prescriptions exist for them.
    return protocolDrugs
        .mapIndexed { index: Int, drugAndDosages: ProtocolDrugAndDosages ->
          val matchingPrescribedDrug = prescribedProtocolDrugs.firstOrNull { it.name == drugAndDosages.drugName }

          ProtocolDrugListItem(
              id = index,
              drugName = drugAndDosages.drugName,
              prescribedDrug = matchingPrescribedDrug,
              hasTopCorners = false,
              medicineFrequencyToFrequencyChoiceItemMap = medicineFrequencyToFrequencyChoiceItemMap
          )
        }
  }
}
