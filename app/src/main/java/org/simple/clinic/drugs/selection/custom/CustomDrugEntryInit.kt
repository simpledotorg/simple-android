package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.drugs.selection.custom.OpenAs.New.FromDrugList
import org.simple.clinic.drugs.selection.custom.OpenAs.New.FromDrugName
import org.simple.clinic.drugs.selection.custom.OpenAs.Update

class CustomDrugEntryInit : Init<CustomDrugEntryModel, CustomDrugEntryEffect> {
  override fun init(model: CustomDrugEntryModel): First<CustomDrugEntryModel, CustomDrugEntryEffect> {
    val updatedModel = when (model.openAs) {
      is FromDrugList, is Update -> model.drugInfoProgressStateLoading()
      is FromDrugName -> model.drugNameLoaded(model.openAs.drugName).drugInfoProgressStateLoaded()
    }

    val effect = when (updatedModel.openAs) {
      is FromDrugList -> FetchDrug(updatedModel.openAs.drugUuid)
      is Update -> FetchPrescription(updatedModel.openAs.prescribedDrugUuid)
      is FromDrugName -> null
    }

    val effects = mutableSetOf(LoadDrugFrequencyChoiceItems, effect)

    return first(updatedModel, effects)
  }
}
