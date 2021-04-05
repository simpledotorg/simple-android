package org.simple.clinic.textInputdatepicker

import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.DismissSheet
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.HideDateErrorMessage
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.ShowDateValidationError
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.Valid
import org.simple.clinic.util.UserInputDatePaddingCharacter

class TextInputDatePickerUpdate(
    private val dateValidator: TextInputDatePickerValidator,
    private val inputDatePaddingCharacter: UserInputDatePaddingCharacter
) : Update<TextInputDatePickerModel, TextInputDatePickerEvent, TextInputDatePickerEffect> {
  override fun update(model: TextInputDatePickerModel, event: TextInputDatePickerEvent): Next<TextInputDatePickerModel, TextInputDatePickerEffect> {
    return when (event) {
      DismissSheetClicked -> dispatch(DismissSheet)
      is DayChanged -> dateChanged(model.dayChanged(event.day))
      is MonthChanged -> dateChanged(model.monthChanged(event.month))
      is YearChanged -> dateChanged(model.yearChanged(event.year))
      DoneClicked -> onDoneClicked(model)
    }
  }

  private fun onDoneClicked(model: TextInputDatePickerModel): Next<TextInputDatePickerModel, TextInputDatePickerEffect> {
    val dateValidation = dateValidator.validate(model.minDate, model.maxDate, getDateText(model))

    return if (dateValidation !is Valid) {
      dispatch(setOf(ShowDateValidationError(dateValidation)))
    } else {
      noChange()
    }
  }

  private fun getDateText(model: TextInputDatePickerModel): String = formatToPaddedDate(model.day, model.month, model.year)

  private fun formatToPaddedDate(day: String, month: String, year: String): String {
    val paddedDd = day.padStart(length = 2, padChar = inputDatePaddingCharacter.value)
    val paddedMm = month.padStart(length = 2, padChar = inputDatePaddingCharacter.value)

    return "$paddedDd/$paddedMm/$year"
  }

  private fun dateChanged(model: TextInputDatePickerModel): Next<TextInputDatePickerModel, TextInputDatePickerEffect> =
      next(model, HideDateErrorMessage)
}
