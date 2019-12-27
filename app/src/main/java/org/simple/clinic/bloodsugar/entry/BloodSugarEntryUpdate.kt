package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.Valid
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class BloodSugarEntryUpdate(
    private val bloodSugarValidator: BloodSugarValidator
) : Update<BloodSugarEntryModel, BloodSugarEntryEvent, BloodSugarEntryEffect> {

  override fun update(
      model: BloodSugarEntryModel,
      event: BloodSugarEntryEvent
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> {
    return when (event) {
      is BloodSugarChanged -> next(model.bloodSugarChanged(event.bloodSugarReading), HideBloodSugarErrorMessage)
      DayChanged -> onDateChanged(model.dayChanged())
      MonthChanged -> onDateChanged(model.monthChanged())
      YearChanged -> onDateChanged(model.yearChanged())
      BackPressed -> dispatch(Dismiss)
      BloodSugarDateClicked -> onBloodSugarDateClicked(model)
      ShowBloodSugarEntryClicked -> dispatch(ShowBloodSugarEntryScreen)
    }
  }

  private fun onBloodSugarDateClicked(
      model: BloodSugarEntryModel
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> {
    val result = bloodSugarValidator.validate(model.bloodSugarReading)
    val effect = if (result is Valid) {
      ShowDateEntryScreen
    } else {
      ShowBloodSugarValidationError
    }
    return dispatch(effect)
  }

  private fun onDateChanged(
      updatedModel: BloodSugarEntryModel
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> =
      next(updatedModel, HideDateErrorMessage)
}
