package org.simple.clinic.textInputdatepicker

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.DismissSheet
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.HideDateErrorMessage
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.ShowDateValidationError
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.UserEnteredDateSelected
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.NotValid.InvalidPattern
import org.simple.clinic.util.UserInputDatePaddingCharacter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class TextInputDatePickerUpdateTest {

  private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  private val updateSpec = UpdateSpec(TextInputDatePickerUpdate(
      TextInputDatePickerValidator(dateFormatter),
      UserInputDatePaddingCharacter.ZERO
  ))
  private val date = LocalDate.parse("2019-04-04")
  private val maxDate = LocalDate.parse("2020-04-04")
  private val defaultModel = TextInputDatePickerModel.create(
      minDate = date,
      maxDate = maxDate,
      prefilledDate = date)

  @Test
  fun `when close button is clicked, then close the sheet`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DismissSheetClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(DismissSheet)
            )
        )
  }

  @Test
  fun `when text changes in the day edittext field, then hide the error message`() {
    val day = "21"

    updateSpec
        .given(defaultModel)
        .whenEvent(DayChanged(day))
        .then(
            assertThatNext(
                hasModel(defaultModel.dayChanged(day)),
                hasEffects(HideDateErrorMessage)
            )
        )
  }

  @Test
  fun `when text changes in the month editText field, then hide the error message`() {
    val month = "08"

    updateSpec
        .given(defaultModel)
        .whenEvent(MonthChanged(month))
        .then(
            assertThatNext(
                hasModel(defaultModel.monthChanged(month)),
                hasEffects(HideDateErrorMessage)
            )
        )
  }

  @Test
  fun `when text changes in the year editText field, then hide the error message`() {
    val year = "2017"

    updateSpec
        .given(defaultModel)
        .whenEvent(YearChanged(year))
        .then(
            assertThatNext(
                hasModel(defaultModel.yearChanged(year)),
                hasEffects(HideDateErrorMessage)
            )
        )
  }

  @Test
  fun `when done button is clicked, then show date validation errors`() {
    val model = defaultModel
        .yearChanged("2019")
        .dayChanged("04")
        .monthChanged("34")

    updateSpec
        .given(model)
        .whenEvent(DoneClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowDateValidationError(InvalidPattern))
            )
        )
  }

  @Test
  fun `when the validation is successful, then send the user entered date back to the previous screen`() {
    val userEnteredDate = LocalDate.of(2020, 4, 3)
    val model = defaultModel
        .dayChanged(userEnteredDate.dayOfMonth.toString())
        .monthChanged(userEnteredDate.month.value.toString())
        .yearChanged(userEnteredDate.year.toString())

    updateSpec
        .given(model)
        .whenEvent(DoneClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(UserEnteredDateSelected(userEnteredDate = userEnteredDate))
            )
        )
  }
}
