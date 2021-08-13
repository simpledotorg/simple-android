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
      is OpenAs.New.FromDrugName -> updatedModelFromDrugName(model, model.openAs.drugName)
      is OpenAs.Update -> first(model, FetchPrescription(model.openAs.prescribedDrugUuid))
    }
  }

  private fun updatedModelFromDrugName(
      model: CustomDrugEntryModel,
      drugName: String
  ): First<CustomDrugEntryModel, CustomDrugEntryEffect> {
    val updatedModel = model.drugNameLoaded(drugName)

    val effects = setOf(SetSheetTitle(drugName, null, null), SetDrugFrequency(null))

    return first(updatedModel, effects)
  }

  private fun updatedModelFromDrug(
      model: CustomDrugEntryModel,
      drug: Drug
  ): First<CustomDrugEntryModel, CustomDrugEntryEffect> {
    val updatedModel = model.drugNameLoaded(drug.name).dosageEdited(drug.dosage).frequencyEdited(drug.frequency)

    return first(updatedModel, SetSheetTitle(drug.name, drug.dosage, drug.frequency), SetDrugFrequency(drug.frequency), SetDrugDosage(drug.dosage))
  }
}
