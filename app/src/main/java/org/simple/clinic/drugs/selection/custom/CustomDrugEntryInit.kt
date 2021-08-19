package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class CustomDrugEntryInit : Init<CustomDrugEntryModel, CustomDrugEntryEffect> {
  override fun init(model: CustomDrugEntryModel): First<CustomDrugEntryModel, CustomDrugEntryEffect> {
    return when (model.openAs) {
      is OpenAs.New.FromDrugList -> first(model, FetchDrug(model.openAs.drugUuid))
      is OpenAs.New.FromDrugName -> updatedModelFromDrugName(model, model.openAs.drugName)
      is OpenAs.Update -> first(model, FetchPrescription(model.openAs.prescribedDrugUuid))
    }
  }

  private fun updatedModelFromDrugName(
      model: CustomDrugEntryModel,
      drugName: String
  ): First<CustomDrugEntryModel, CustomDrugEntryEffect> {
    val updatedModel = model.drugNameLoaded(drugName)

    val effects = setOf(SetSheetTitle(drugName, null, null))

    return first(updatedModel, effects)
  }
}
