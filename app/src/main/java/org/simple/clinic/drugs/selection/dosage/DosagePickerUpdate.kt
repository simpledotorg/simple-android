package org.simple.clinic.drugs.selection.dosage

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class DosagePickerUpdate : Update<DosagePickerModel, DosagePickerEvent, DosagePickerEffect> {

  override fun update(model: DosagePickerModel, event: DosagePickerEvent): Next<DosagePickerModel, DosagePickerEffect> {
    return noChange()
  }
}
