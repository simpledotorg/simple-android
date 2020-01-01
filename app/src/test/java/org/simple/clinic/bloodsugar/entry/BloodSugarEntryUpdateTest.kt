package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet.ScreenType.BLOOD_SUGAR_ENTRY
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet.ScreenType.DATE_ENTRY
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooHigh
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class BloodSugarEntryUpdateTest {

  private val bloodSugarValidator = BloodSugarValidator()
  private val testUserClock = TestUserClock()
  private val dateValidator = UserInputDateValidator(testUserClock, DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))

  private val validBloodSugar = "30"
  private val invalidBloodSugar = "1001"
  private val patientUuid = UUID.fromString("79145baf-7a5c-4442-ab30-2da564a32944")
  private val validBloodSugarDate = LocalDate.of(1994, 2, 14)

  private val defaultModel = BloodSugarEntryModel.create(LocalDate.now(testUserClock).year, New(patientUuid, Random))
  private val updateSpec = UpdateSpec<BloodSugarEntryModel, BloodSugarEntryEvent, BloodSugarEntryEffect>(
      BloodSugarEntryUpdate(
          bloodSugarValidator,
          dateValidator,
          LocalDate.now(testUserClock.zone),
          UserInputDatePaddingCharacter.ZERO
      )
  )

  @Test
  fun `when blood sugar value changes, hide any blood sugar error message`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(BloodSugarChanged(validBloodSugar))
        .then(assertThatNext(
            hasModel(defaultModel.bloodSugarChanged(validBloodSugar)),
            hasEffects(HideBloodSugarErrorMessage as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when date values change, hide any date error message`() {
    val day = validBloodSugarDate.dayOfMonth.toString()
    val month = validBloodSugarDate.monthValue.toString()
    val year = validBloodSugarDate.year.toString().substring(2)

    updateSpec
        .given(defaultModel)
        .whenEvents(
            DayChanged(day),
            MonthChanged(month),
            YearChanged(year)
        )
        .then(assertThatNext(
            hasModel(
                defaultModel
                    .dayChanged(day)
                    .monthChanged(month)
                    .yearChanged(year)
            ),
            hasEffects(HideDateErrorMessage as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when blood sugar entry is active and back is pressed, then the sheet should be dismissed`() {
    updateSpec
        .given(defaultModel.screenChanged(BLOOD_SUGAR_ENTRY))
        .whenEvent(BackPressed)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(Dismiss as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when date entry is active and back is pressed and has valid date, then the blood sugar entry screen should be shown`() {
    updateSpec
        .given(
            defaultModel
                .screenChanged(DATE_ENTRY)
                .dayChanged(validBloodSugarDate.dayOfMonth.toString())
                .monthChanged(validBloodSugarDate.monthValue.toString())
                .yearChanged(validBloodSugarDate.year.toString().substring(2))
        )
        .whenEvent(BackPressed)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBloodSugarEntryScreen(validBloodSugarDate) as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when date entry is active, date is invalid and back is pressed, then the validation error should be shown`() {
    val day = "14"
    val month = "13"
    val year = "94"
    val model = defaultModel
        .screenChanged(DATE_ENTRY)
        .dayChanged(day)
        .monthChanged(month)
        .yearChanged(year)

    updateSpec
        .given(model)
        .whenEvent(BackPressed)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowDateValidationError(InvalidPattern) as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when blood sugar entry is active and value is valid and date button is pressed, then date entry should be shown`() {
    updateSpec
        .given(defaultModel.bloodSugarChanged(validBloodSugar))
        .whenEvent(BloodSugarDateClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowDateEntryScreen as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when blood sugar entry is active and value is invalid and date button is pressed, then show blood sugar validation errors`() {
    updateSpec
        .given(defaultModel.bloodSugarChanged(invalidBloodSugar))
        .whenEvent(BloodSugarDateClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBloodSugarValidationError(ErrorBloodSugarTooHigh) as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when date entry screen is active and has valid date and sheet back button is pressed, then show blood sugar entry sheet`() {
    updateSpec
        .given(
            defaultModel
                .screenChanged(DATE_ENTRY)
                .dayChanged(validBloodSugarDate.dayOfMonth.toString())
                .monthChanged(validBloodSugarDate.monthValue.toString())
                .yearChanged(validBloodSugarDate.year.toString().substring(2))
        )
        .whenEvent(ShowBloodSugarEntryClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBloodSugarEntryScreen(validBloodSugarDate) as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when blood sugar sheet has invalid date and sheet back button is pressed, then show date validation errors`() {
    val day = "14"
    val month = "13"
    val year = "94"
    val model = defaultModel
        .screenChanged(DATE_ENTRY)
        .dayChanged(day)
        .monthChanged(month)
        .yearChanged(year)

    updateSpec
        .given(model)
        .whenEvent(ShowBloodSugarEntryClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowDateValidationError(InvalidPattern) as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when save is clicked and there are no validation errors, then save the new blood sugar`() {
    val validBloodSugarModel = defaultModel
        .bloodSugarChanged(validBloodSugar)
        .dayChanged(validBloodSugarDate.dayOfMonth.toString())
        .monthChanged(validBloodSugarDate.monthValue.toString())
        .yearChanged(validBloodSugarDate.year.toString().substring(2))
        .datePrefilled(validBloodSugarDate)

    updateSpec
        .given(validBloodSugarModel)
        .whenEvent(SaveClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CreateNewBloodSugarEntry(
                patientUuid,
                validBloodSugar.toInt(),
                Random,
                validBloodSugarDate,
                validBloodSugarDate
            ) as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when save is clicked and there are validation errors, then show validation errors`() {
    val invalidBloodSugarModel = defaultModel
        .bloodSugarChanged(invalidBloodSugar)
        .dayChanged(validBloodSugarDate.dayOfMonth.toString())
        .monthChanged(validBloodSugarDate.monthValue.toString())
        .yearChanged(validBloodSugarDate.year.toString().substring(2))
        .datePrefilled(validBloodSugarDate)

    updateSpec
        .given(invalidBloodSugarModel)
        .whenEvent(SaveClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBloodSugarValidationError(ErrorBloodSugarTooHigh) as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when blood sugar is saved, then set blood sugar saved result and dispatch`() {
    val validBloodSugarModel = defaultModel
        .bloodSugarChanged(validBloodSugar)
        .dayChanged(validBloodSugarDate.dayOfMonth.toString())
        .monthChanged(validBloodSugarDate.monthValue.toString())
        .yearChanged(validBloodSugarDate.year.toString().substring(2))
        .datePrefilled(validBloodSugarDate)
    val wasDateChanged = false

    updateSpec
        .given(validBloodSugarModel)
        .whenEvent(BloodSugarSaved(wasDateChanged))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetBloodSugarSavedResultAndFinish as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when date is prefilled, then update the model`() {
    val prefilledDate = LocalDate.now(testUserClock)

    updateSpec
        .given(defaultModel)
        .whenEvent(DatePrefilled(prefilledDate))
        .then(assertThatNext(
            hasModel(defaultModel.datePrefilled(prefilledDate)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when screen is changed, then update the active screen in model`() {
    val activeScreen = DATE_ENTRY

    updateSpec
        .given(defaultModel)
        .whenEvent(ScreenChanged(activeScreen))
        .then(assertThatNext(
            hasModel(defaultModel.screenChanged(activeScreen)),
            hasNoEffects()
        ))
  }
}
