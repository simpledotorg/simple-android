package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.drugs.search.Drug
import org.simple.clinic.mobius.first

class CustomDrugEntryInit : Init<CustomDrugEntryModel, CustomDrugEntryEffect> {
  override fun init(model: CustomDrugEntryModel): First<CustomDrugEntryModel, CustomDrugEntryEffect> {
    return when (model.openAs) {
      is OpenAs.New.FromDrugList -> updatedModelFromDrug(model, model.openAs.drug)
      else -> first(model)
    }
  }

  private fun updatedModelFromDrug(
      model: CustomDrugEntryModel,
      drug: Drug
  ): First<CustomDrugEntryModel, CustomDrugEntryEffect> {
    val updatedModel = model.drugNameLoaded(drug.name).dosageEdited(drug.dosage).frequencyEdited(drug.frequency)

    return first(updatedModel, SetSheetTitle(drug.name, drug.dosage, drug.frequency), SetDrugFrequency(drug.frequency), SetDrugDosage(drug.dosage))
  }
}
