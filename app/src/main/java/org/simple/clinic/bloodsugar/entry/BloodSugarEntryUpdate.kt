package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class BloodSugarEntryUpdate : Update<BloodSugarEntryModel, BloodSugarEntryEvent, BloodSugarEntryEffect> {

  override fun update(
      model: BloodSugarEntryModel,
      event: BloodSugarEntryEvent
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> {
    return next(model.bloodSugarChanged(), HideBloodSugarErrorMessage)
  }
}
