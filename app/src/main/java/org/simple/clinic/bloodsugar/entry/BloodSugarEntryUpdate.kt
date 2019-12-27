package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class BloodSugarEntryUpdate : Update<BloodSugarEntryModel, BloodSugarEntryEvent, BloodSugarEntryEffect> {

  override fun update(
      model: BloodSugarEntryModel,
      event: BloodSugarEntryEvent
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> {
    return when (event) {
      BloodSugarChanged -> next(model.bloodSugarChanged(), HideBloodSugarErrorMessage)
      DayChanged -> onDateChanged(model.dayChanged())
      MonthChanged -> onDateChanged(model.monthChanged())
      YearChanged -> onDateChanged(model.yearChanged())
    }
  }

  private fun onDateChanged(
      updatedModel: BloodSugarEntryModel
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> =
      next(updatedModel, HideDateErrorMessage)
}
