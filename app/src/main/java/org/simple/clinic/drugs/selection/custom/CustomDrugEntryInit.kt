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

    val effects = mutableSetOf<CustomDrugEntryEffect>(LoadDrugFrequencyChoiceItems)
    when (model.openAs) {
      is FromDrugList -> effects.add(FetchDrug(model.openAs.drugUuid))
      is Update -> effects.add(FetchPrescription(model.openAs.prescribedDrugUuid))
      is FromDrugName -> {
        /* no-op */
      }
    }

    return first(updatedModel, effects)
  }
}
