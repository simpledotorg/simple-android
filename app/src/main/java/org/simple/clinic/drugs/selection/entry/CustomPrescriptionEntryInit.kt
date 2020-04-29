package org.simple.clinic.drugs.selection.entry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class CustomPrescriptionEntryInit : Init<CustomPrescriptionEntryModel, CustomPrescriptionEntryEffect> {

  override fun init(model: CustomPrescriptionEntryModel): First<CustomPrescriptionEntryModel, CustomPrescriptionEntryEffect> {
    return first(model)
  }
}
