package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.Valid
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result
import org.threeten.bp.LocalDate

class BloodSugarEntryUpdate(
    private val bloodSugarValidator: BloodSugarValidator,
    private val dateValidator: UserInputDateValidator,
    private val dateInUserTimeZone: LocalDate,
    private val inputDatePaddingCharacter: UserInputDatePaddingCharacter
) : Update<BloodSugarEntryModel, BloodSugarEntryEvent, BloodSugarEntryEffect> {

  override fun update(
      model: BloodSugarEntryModel,
      event: BloodSugarEntryEvent
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> {
    return when (event) {
      is BloodSugarChanged -> next(model.bloodSugarChanged(event.bloodSugarReading), HideBloodSugarErrorMessage)
      is DayChanged -> onDateChanged(model.dayChanged(event.day))
      is MonthChanged -> onDateChanged(model.monthChanged(event.month))
      is YearChanged -> onDateChanged(model.yearChanged(event.twoDigitYear))
      BackPressed -> dispatch(Dismiss)
      BloodSugarDateClicked -> onBloodSugarDateClicked(model)
      ShowBloodSugarEntryClicked -> showBloodSugarClicked(model)
    }
  }

  private fun showBloodSugarClicked(
      model: BloodSugarEntryModel
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> {
    val result = dateValidator.validate(getDateText(model), dateInUserTimeZone)
    val effect = if (result is Result.Valid) {
      ShowBloodSugarEntryScreen
    } else {
      ShowDateValidationError
    }
    return dispatch(effect)
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

  private fun getDateText(model: BloodSugarEntryModel) =
      formatToPaddedDate(model.day, model.month, model.twoDigitYear, model.year)

  private fun formatToPaddedDate(day: String, month: String, twoDigitYear: String, fourDigitYear: String): String {
    val paddedDd = day.padStart(length = 2, padChar = inputDatePaddingCharacter.value)
    val paddedMm = month.padStart(length = 2, padChar = inputDatePaddingCharacter.value)
    val paddedYy = twoDigitYear.padStart(length = 2, padChar = inputDatePaddingCharacter.value)

    val firstTwoDigitsOfYear = fourDigitYear.substring(0, 2)
    val paddedYyyy = firstTwoDigitsOfYear + paddedYy
    return "$paddedDd/$paddedMm/$paddedYyyy"
  }
}
