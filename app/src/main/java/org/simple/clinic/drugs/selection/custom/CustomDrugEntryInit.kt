package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class CustomDrugEntryInit : Init<CustomDrugEntryModel, CustomDrugEntryEffect> {
  override fun init(model: CustomDrugEntryModel): First<CustomDrugEntryModel, CustomDrugEntryEffect> {
    return when (model.openAs) {
      is OpenAs.New -> first(model)
      is OpenAs.Update -> first(model, FetchPrescription(model.openAs.prescribedDrugUuid))
    }
  }
}