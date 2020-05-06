package org.simple.clinic.drugs.selection.entry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class CustomPrescriptionEntryInit : Init<CustomPrescriptionEntryModel, CustomPrescriptionEntryEffect> {

  override fun init(model: CustomPrescriptionEntryModel): First<CustomPrescriptionEntryModel, CustomPrescriptionEntryEffect> {
    return when (model.openAs) {
      is OpenAs.New -> first(model)
      is OpenAs.Update -> first(model, FetchPrescription(model.openAs.prescribedDrugUuid))
    }
  }
}
