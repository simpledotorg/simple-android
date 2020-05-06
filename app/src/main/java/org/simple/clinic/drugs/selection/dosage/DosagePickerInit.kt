package org.simple.clinic.drugs.selection.dosage

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class DosagePickerInit : Init<DosagePickerModel, DosagePickerEffect> {

  override fun init(model: DosagePickerModel): First<DosagePickerModel, DosagePickerEffect> {
    return first(model)
  }
}
