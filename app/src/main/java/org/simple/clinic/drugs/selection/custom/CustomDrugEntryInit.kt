package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class CustomDrugEntryInit : Init<CustomDrugEntryModel, CustomDrugEntryEffect> {
  override fun init(model: CustomDrugEntryModel): First<CustomDrugEntryModel, CustomDrugEntryEffect> {
    return when (model.openAs) {
      is OpenAs.New.FromDrugList -> first(model.customDrugEntryProgressStateLoading(), FetchDrug(model.openAs.drugUuid), LoadDrugFrequencyChoiceItems)
      is OpenAs.New.FromDrugName -> first(model.drugNameLoaded(model.openAs.drugName).customDrugEntryProgressStateLoaded(), LoadDrugFrequencyChoiceItems)
      is OpenAs.Update -> first(model.customDrugEntryProgressStateLoading(), FetchPrescription(model.openAs.prescribedDrugUuid), LoadDrugFrequencyChoiceItems)
    }
  }
}
