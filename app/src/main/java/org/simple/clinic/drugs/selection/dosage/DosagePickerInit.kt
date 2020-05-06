package org.simple.clinic.drugs.selection.dosage

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class DosagePickerInit : Init<DosagePickerModel, DosagePickerEffect> {

  override fun init(model: DosagePickerModel): First<DosagePickerModel, DosagePickerEffect> {
    val effects = mutableSetOf<DosagePickerEffect>()

    if (!model.hasLoadedProtocolDrugs) {
      effects.add(LoadProtocolDrugsByName(model.drugName))
    }

    return first(model, effects)
  }
}
